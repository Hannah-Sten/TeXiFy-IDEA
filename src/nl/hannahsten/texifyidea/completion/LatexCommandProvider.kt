package nl.hannahsten.texifyidea.completion

import com.google.common.base.Strings
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.indexing.FileBasedIndex
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexCommandArgumentInsertHandler
import nl.hannahsten.texifyidea.completion.handlers.LatexMathInsertHandler
import nl.hannahsten.texifyidea.completion.handlers.LatexNoMathInsertHandler
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.index.file.LatexPackageIndex
import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.Kindness.getKindWords
import nl.hannahsten.texifyidea.util.files.*
import java.util.*
import java.util.stream.Collectors

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexCommandProvider internal constructor(private val mode: LatexMode) :
    CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        when (mode) {
            LatexMode.NORMAL -> {
                addNormalCommands(result)
                addCustomCommands(parameters, result)
                addIndexedCommands(result, parameters)
            }
            LatexMode.MATH -> {
                addMathCommands(result)
                addCustomCommands(parameters, result, LatexMode.MATH)
            }
            LatexMode.ENVIRONMENT_NAME -> addEnvironments(result, parameters)
        }
        result.addLookupAdvertisement("Don't use \\\\ outside of tabular or math mode, it's evil.")
    }

    private fun addIndexedCommands(result: CompletionResultSet, parameters: CompletionParameters) {
        result.addAllElements(
            FileBasedIndex.getInstance().getAllKeys(LatexPackageIndex.id, parameters.editor.project ?: return)
                .map { cmdWithSlash ->
                    val cmd = cmdWithSlash.substring(1)
                    val files = FileBasedIndex.getInstance().getContainingFiles(
                        LatexPackageIndex.id,
                        cmd,
                        GlobalSearchScope.projectScope(parameters.editor.project ?: return)
                    )
                    // todo use files for insertion handler
                    LookupElementBuilder.create(cmd, cmd)
                        .withPresentableText(cmdWithSlash)
                        .bold()
                        .withInsertHandler(LatexNoMathInsertHandler())
                        .withIcon(TexifyIcons.DOT_COMMAND)
                }
        )
    }

    private fun addNormalCommands(result: CompletionResultSet) {
        result.addAllElements(
            LatexRegularCommand.values().flatMap { cmd ->
                cmd.arguments.toSet().optionalPowerSet().mapIndexed { index, args ->
                    LookupElementBuilder.create(cmd, cmd.command + List(index) { " " }.joinToString(""))
                        .withPresentableText(cmd.commandDisplay)
                        .bold()
                        .withTailText(args.joinToString("") + " " + packageName(cmd), true)
                        .withTypeText(cmd.display)
                        .withInsertHandler(LatexNoMathInsertHandler(args.toList()))
                        .withIcon(TexifyIcons.DOT_COMMAND)
                }
            }
        )
        result.addLookupAdvertisement(getKindWords())
    }

    private fun addMathCommands(result: CompletionResultSet) {
        // Find all commands.
        val commands: MutableList<LatexCommand> = ArrayList()
        Collections.addAll(commands, *LatexMathCommand.values())
        commands.add(LatexRegularCommand.BEGIN)

        // Create autocomplete elements.
        result.addAllElements(
            commands.flatMap { cmd: LatexCommand ->
                cmd.arguments.toSet().optionalPowerSet().mapIndexed { index, args ->
                    val handler = if (cmd is LatexRegularCommand) LatexNoMathInsertHandler(args.toList())
                    else LatexMathInsertHandler(args.toList())
                    LookupElementBuilder.create(cmd, cmd.command + List(index) { " " }.joinToString(""))
                        .withPresentableText(cmd.commandDisplay)
                        .bold()
                        .withTailText(args.joinToString("") + " " + packageName(cmd), true)
                        .withTypeText(cmd.display)
                        .withInsertHandler(handler)
                        .withIcon(TexifyIcons.DOT_COMMAND)
                }
            }
        )
    }

    private fun addEnvironments(result: CompletionResultSet, parameters: CompletionParameters) {
        // Find all environments.
        val environments: MutableList<Environment> = ArrayList()
        Collections.addAll(environments, *DefaultEnvironment.values())
        LatexDefinitionIndex.getItemsInFileSet(parameters.originalFile).stream()
            .filter { cmd -> Magic.Command.environmentDefinitions.contains(cmd.name) }
            .map { cmd -> cmd.requiredParameter(0) }
            .filter { obj -> Objects.nonNull(obj) }
            .map { environmentName -> SimpleEnvironment(environmentName!!) }
            .forEach { e: SimpleEnvironment -> environments.add(e) }

        // Create autocomplete elements.
        result.addAllElements(
            ContainerUtil.map2List(environments) { env: Environment ->
                LookupElementBuilder.create(env, env.environmentName)
                    .withPresentableText(env.environmentName)
                    .bold()
                    .withTailText(env.getArgumentsDisplay() + " " + packageName(env), true)
                    .withIcon(TexifyIcons.DOT_ENVIRONMENT)
            }
        )
        result.addLookupAdvertisement(getKindWords())
    }

    private fun packageName(dependend: Dependend): String {
        val name = dependend.dependency.name
        return if ("" == name) {
            ""
        }
        else " ($name)"
    }

    private fun addCustomCommands(
        parameters: CompletionParameters, result: CompletionResultSet,
        mode: LatexMode? = null
    ) {
        val project = parameters.editor.project ?: return
        val file = parameters.originalFile
        val files: MutableSet<PsiFile> = HashSet(file.referencedFileSet())
        val root = file.findRootFile()
        val documentClass = root.documentClassFileInProject()
        if (documentClass != null) {
            files.add(documentClass)
        }
        val searchFiles = files.stream()
            .map { obj: PsiFile -> obj.virtualFile }
            .collect(Collectors.toSet())
        searchFiles.add(file.virtualFile)
        val scope = GlobalSearchScope.filesScope(project, searchFiles)
        val cmds = LatexCommandsIndex.getItems(project, scope)
        for (cmd in cmds) {
            if (!cmd.isDefinition() && !cmd.isEnvironmentDefinition()) {
                continue
            }
            if (mode !== LatexMode.MATH && cmd.name in Magic.Command.mathCommandDefinitions) {
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
        if (commands.commandToken.text in Magic.Command.commandDefinitions) {
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
                val optional: List<String> = LinkedList(commands.optionalParameters.keys)
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
                (if (optional.size == 2) "[args]" else "") + Strings.repeat("{param}", requiredParameterCount)
            }

            "\\DeclarePairedDelimiter" -> "{param}"
            "\\DeclarePairedDelimiterX", "\\DeclarePairedDelimiterXPP" -> {
                val optional = commands.optionalParameters.keys.firstOrNull()
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
                    if (Magic.Package.xparseParamSpecifiers[c] ?: return@map "") "{param}"
                    else "[]"
                }.joinToString("")
            }

            else -> ""
        }
    }

    private fun getCommandName(commands: LatexCommands): String? {
        return when (commands.name) {
            in Magic.Command.mathCommandDefinitions + setOf("\\newcommand", "\\newif") -> getNewCommandName(commands)
            else -> getDefinitionName(commands)
        }
    }

    private fun getNewCommandName(commands: LatexCommands) = commands.forcedFirstRequiredParameterAsCommand()?.name

    private fun getDefinitionName(commands: LatexCommands) = commands.definitionCommand()?.commandToken?.text
}