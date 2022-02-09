package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.LatexEnvironmentProvider.addEnvironments
import nl.hannahsten.texifyidea.completion.LatexEnvironmentProvider.addIndexedEnvironments
import nl.hannahsten.texifyidea.completion.LatexEnvironmentProvider.packageName
import nl.hannahsten.texifyidea.completion.handlers.LatexCommandArgumentInsertHandler
import nl.hannahsten.texifyidea.completion.handlers.LatexMathInsertHandler
import nl.hannahsten.texifyidea.completion.handlers.LatexNoMathInsertHandler
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex
import nl.hannahsten.texifyidea.lang.LatexMode
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.*
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.toStringMap
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.Kindness.getKindWords
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PackageMagic
import java.util.*

/**
 * Provide autocompletion for commands and environments.
 *
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexCommandsAndEnvironmentsCompletionProvider internal constructor(private val mode: LatexMode) :
    CompletionProvider<CompletionParameters>() {

    companion object {

        /** Cache for commands which are indexed and which should be added to the autocompletion. */
        val indexedCommands = mutableSetOf<LookupElementBuilder>()
    }

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        when (mode) {
            LatexMode.NORMAL -> {
                addIndexedCommands(result, parameters)
                addNormalCommands(result, parameters.editor.project ?: return)
                addCustomCommands(parameters, result)
            }
            LatexMode.MATH -> {
                addMathCommands(result)
                addCustomCommands(parameters, result, LatexMode.MATH)
            }
            LatexMode.ENVIRONMENT_NAME -> {
                addEnvironments(result, parameters)
                addIndexedEnvironments(result, parameters)
            }
        }
        result.addLookupAdvertisement("Don't use \\\\ outside of tabular or math mode, it's evil.")
    }

    private fun createCommandLookupElements(cmd: LatexCommand): List<LookupElementBuilder> {
        return cmd.arguments.toSet().optionalPowerSet().mapIndexed { index, args ->
            // Add spaces to the lookup text to distinguish different versions of commands within the same package (optional parameters).
            // Add the package name to the lookup text so we can distinguish between the same commands that come from different packages.
            // This 'extra' text will be automatically inserted by intellij and is removed by the LatexCommandArgumentInsertHandler after insertion.
            val default = cmd.dependency.isDefault
            LookupElementBuilder.create(cmd, cmd.command + " ".repeat(index + default.not().int) + cmd.dependency.displayString)
                .withPresentableText(cmd.commandWithSlash)
                .bold()
                .withTailText(args.joinToString("") + " " + packageName(cmd), true)
                .withTypeText(cmd.display)
                .withInsertHandler(LatexNoMathInsertHandler(args.toList()))
                .withIcon(TexifyIcons.DOT_COMMAND)
        }
    }

    /**
     * Add all indexed commands to the autocompletion.
     */
    private fun addIndexedCommands(result: CompletionResultSet, parameters: CompletionParameters) {
        // Use cache if available
        if (indexedCommands.isNotEmpty()) {
            result.addAllElements(indexedCommands)
            return
        }

        // If using texlive, filter on commands which are in packages included in the project
        // The reason for doing this, is that the user probably is using texlive-full, in which case the
        // completion would be flooded with duplicate commands from packages that nobody uses.
        // For example, the (initially) first suggestion for \enquote is the version from the aiaa package, which is unlikely to be correct.
        // Therefore, we limit ourselves to packages included somewhere in the project (directly or indirectly).
        val project = parameters.editor.project ?: return

        // If TeX Live is available at all, either implicitly or explicitly, it could be in the index, so we filter commands
        val usesTexlive = TexliveSdk.isAvailable || ProjectJdkTable.getInstance().allJdks.any { it.sdkType is TexliveSdk }

        val packagesInProject = if (!usesTexlive) emptyList() else includedPackages(LatexIncludesIndex.getItems(project), project).plus(LatexPackage.DEFAULT)

        val commands = mutableSetOf<LookupElementBuilder>()
        FileBasedIndex.getInstance().getAllKeys(LatexExternalCommandIndex.id, project).toSet()
            .forEach { cmdWithSlash ->
                val cmdWithoutSlash = cmdWithSlash.substring(1)
                LatexCommand.lookupInIndex(cmdWithoutSlash, project)
                    .filter { if (usesTexlive) it.dependency in packagesInProject else true }
                    .forEach { cmd ->
                    createCommandLookupElements(cmd)
                        // Avoid duplicates of commands defined in LaTeX base, because they are often very similar commands defined in different document classes, so it makes not
                        // much sense at the moment to have them separately in the autocompletion.
                        // Effectively this results in just taking the first one we found
                        .filter { newBuilder ->
                            if (cmd.dependency.isDefault) {
                                commands.none { it.lookupString == newBuilder.lookupString }
                            }
                            else {
                                true
                            }
                        }
                        .forEach { commands.add(it) }
                }
            }
        indexedCommands.addAll(commands)
        result.addAllElements(commands)
    }

    private fun addNormalCommands(result: CompletionResultSet, project: Project) {
        val indexedKeys = FileBasedIndex.getInstance().getAllKeys(LatexExternalCommandIndex.id, project)

        result.addAllElements(
            LatexRegularCommand.values().flatMap { cmd ->
                /** True if there is a package for which we already have the [cmd] command indexed.  */
                fun alreadyIndexed() =
                    FileBasedIndex.getInstance().getContainingFiles(LatexExternalCommandIndex.id, cmd.commandWithSlash, GlobalSearchScope.everythingScope(project))
                        .map { LatexPackage.create(it) }.contains(cmd.dependency)

                // Avoid adding duplicates
                // Prefer the indexed command (if it really is the same one), as that one has documentation
                if (cmd.commandWithSlash in indexedKeys && alreadyIndexed()) {
                    emptyList()
                }
                else {
                    createCommandLookupElements(cmd)
                }
            }
        )
        result.addLookupAdvertisement(getKindWords())
    }

    private fun addMathCommands(result: CompletionResultSet) {
        // Find all commands.
        val commands: MutableList<LatexCommand> = ArrayList(LatexMathCommand.values())
        commands.add(LatexGenericRegularCommand.BEGIN)

        // Create autocomplete elements.
        result.addAllElements(
            commands.flatMap { cmd: LatexCommand ->
                cmd.arguments.toSet().optionalPowerSet().mapIndexed { index, args ->
                    val handler = if (cmd.isMathMode.not()) LatexNoMathInsertHandler(args.toList())
                    else LatexMathInsertHandler(args.toList())
                    LookupElementBuilder.create(cmd, cmd.command + List(index) { " " }.joinToString(""))
                        .withPresentableText(cmd.commandWithSlash)
                        .bold()
                        .withTailText(args.joinToString("") + " " + packageName(cmd), true)
                        .withTypeText(cmd.display)
                        .withInsertHandler(handler)
                        .withIcon(TexifyIcons.DOT_COMMAND)
                }
            }
        )
    }

    private fun addCustomCommands(
        parameters: CompletionParameters, result: CompletionResultSet,
        mode: LatexMode? = null
    ) {
        val file = parameters.originalFile
        val files: MutableSet<PsiFile> = HashSet(file.referencedFileSet())
        val root = file.findRootFile()
        val documentClass = root.documentClassFileInProject()
        if (documentClass != null) {
            files.add(documentClass)
        }
        val cmds = getCommandsInFiles(files, file)
        for (cmd in cmds) {
            if (!cmd.isDefinition() && !cmd.isEnvironmentDefinition()) {
                continue
            }
            if (mode !== LatexMode.MATH && cmd.name in CommandMagic.mathCommandDefinitions) {
                continue
            }
            val cmdName = getCommandName(cmd) ?: continue

            // Skip over 'private' commands containing @ symbol in normal tex source files.
            if (!file.isClassFile() && !file.isStyleFile()) {
                if (cmdName.contains("@")) {
                    continue
                }
            }
            val arguments = getArgumentsFromDefinition(cmd)
            var typeText = getTypeText(cmd)
            val line = 1 + StringUtil.offsetToLineNumber(cmd.containingFile.text, cmd.textOffset)
            typeText = typeText + " " + cmd.containingFile.name + ":" + line
            result.addAllElements(
                arguments.toSet().optionalPowerSet().mapIndexed { index, args ->
                    LookupElementBuilder.create(cmd, cmdName.substring(1) + List(index) { " " }.joinToString(""))
                        .withPresentableText(cmdName)
                        .bold()
                        .withTailText(args.joinToString(""), true)
                        .withTypeText(typeText, true)
                        .withInsertHandler(LatexCommandArgumentInsertHandler(args.toList()))
                        .withIcon(TexifyIcons.DOT_COMMAND)
                }
            )
        }
        result.addLookupAdvertisement(getKindWords())
    }

    private fun getArgumentsFromDefinition(commands: LatexCommands): List<Argument> {
        val argumentString = getTailText(commands)
        val argumentStrings =
            """(\[[\w\d]*])|(\{[\w\d]*})""".toRegex().findAll(argumentString).map { it.value }.toList()
        return argumentStrings.map {
            if (it.startsWith("{")) RequiredArgument(it.drop(1).dropLast(1))
            else OptionalArgument(it.drop(1).dropLast(1))
        }
    }

    private fun getTypeText(commands: LatexCommands): String {
        if (commands.commandToken.text in CommandMagic.commandDefinitionsAndRedefinitions) {
            return ""
        }
        val firstNext = commands.nextCommand() ?: return ""
        val secondNext = firstNext.nextCommand() ?: return ""
        val lookup = secondNext.commandToken.text
        return lookup ?: ""
    }

    private fun getTailText(commands: LatexCommands): String {
        return when (commands.commandToken.text) {
            "\\newcommand" -> {
                val optional: List<String> = LinkedList(commands.optionalParameterMap.toStringMap().keys)
                var requiredParameterCount = 0
                if (optional.isNotEmpty()) {
                    try {
                        when (optional.size) {
                            1 -> requiredParameterCount = optional[0].toInt()
                            2 -> requiredParameterCount = optional[0].toInt() - 1
                        }
                    }
                    catch (ignore: NumberFormatException) {
                    }
                }

                (if (optional.size == 2) "[args]" else "") +
                        if (requiredParameterCount == 1) "{param}"
                        // Number the required parameters so a toSet call won't merge them. This way the
                        // user can keep them apart as well.
                        else (1..requiredParameterCount).joinToString("") { "{param$it}" }
            }

            "\\DeclarePairedDelimiter" -> "{param}"
            "\\DeclarePairedDelimiterX", "\\DeclarePairedDelimiterXPP" -> {
                val optional = commands.optionalParameterMap.toStringMap().keys.firstOrNull()
                val nrParams = try {
                    optional?.toInt() ?: 0
                }
                catch (ignore: java.lang.NumberFormatException) {
                    0
                }
                (1..nrParams).joinToString("") { "{param}" }
            }

            "\\NewDocumentCommand", "\\DeclareDocumentCommand" -> {
                val paramSpecification = commands.requiredParameters.getOrNull(1)?.removeAll("null", " ") ?: ""
                paramSpecification.map { c ->
                    if (PackageMagic.xparseParamSpecifiers[c] ?: return@map "") "{param}"
                    else "[]"
                }.joinToString("")
            }

            else -> ""
        }
    }

    private fun getCommandName(commands: LatexCommands): String? {
        return when (commands.name) {
            in CommandMagic.mathCommandDefinitions + setOf("\\newcommand", "\\newif") -> getNewCommandName(commands)
            else -> getDefinitionName(commands)
        }
    }

    private fun getNewCommandName(commands: LatexCommands) = commands.forcedFirstRequiredParameterAsCommand()?.name

    private fun getDefinitionName(commands: LatexCommands) = commands.definitionCommand()?.commandToken?.text
}