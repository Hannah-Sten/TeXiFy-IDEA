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
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexCommandArgumentInsertHandler
import nl.hannahsten.texifyidea.completion.handlers.LatexMathInsertHandler
import nl.hannahsten.texifyidea.completion.handlers.LatexNoMathInsertHandler
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.Kindness.getKindWords
import nl.hannahsten.texifyidea.util.files.*
import java.util.*
import java.util.stream.Collectors
import kotlin.math.min

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexCommandProvider internal constructor(private val mode: LatexMode) : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters,
                                context: ProcessingContext, result: CompletionResultSet) {
        when (mode) {
            LatexMode.NORMAL -> {
                addNormalCommands(result)
                addCustomCommands(parameters, result)
            }
            LatexMode.MATH -> {
                addMathCommands(result)
                addCustomCommands(parameters, result, LatexMode.MATH)
            }
            LatexMode.ENVIRONMENT_NAME -> addEnvironments(result, parameters)
        }
        result.addLookupAdvertisement("Don't use \\\\ outside of tabular or math mode, it's evil.")
    }

    private fun addNormalCommands(result: CompletionResultSet) {
        result.addAllElements(ContainerUtil.map2List(
                LatexRegularCommand.values()
        ) { cmd: LatexRegularCommand ->
            LookupElementBuilder.create(cmd, cmd.command)
                    .withPresentableText(cmd.commandDisplay)
                    .bold()
                    .withTailText(cmd.getArgumentsDisplay() + " " + packageName(cmd), true)
                    .withTypeText(cmd.display)
                    .withInsertHandler(LatexNoMathInsertHandler())
                    .withIcon(TexifyIcons.DOT_COMMAND)
        })
        result.addElement(LookupElementBuilder.create("abcdefghijklmnopqrstuvwxyz"))
        result.addLookupAdvertisement(getKindWords())
    }

    private fun addMathCommands(result: CompletionResultSet) {
        // Find all commands.
        val commands: MutableList<LatexCommand> = ArrayList()
        Collections.addAll(commands, *LatexMathCommand.values())
        commands.add(LatexRegularCommand.BEGIN)

        // Create autocomplete elements.
        result.addAllElements(ContainerUtil.map2List(
                commands
        ) { cmd: LatexCommand ->
            val handler = if (cmd is LatexRegularCommand) LatexNoMathInsertHandler() else LatexMathInsertHandler()
            LookupElementBuilder.create(cmd, cmd.command)
                    .withPresentableText(cmd.commandDisplay)
                    .bold()
                    .withTailText(cmd.getArgumentsDisplay() + " " + packageName(cmd), true)
                    .withTypeText(cmd.display)
                    .withInsertHandler(handler)
                    .withIcon(TexifyIcons.DOT_COMMAND)
        })
    }

    private fun addEnvironments(result: CompletionResultSet, parameters: CompletionParameters) {
        // Find all environments.
        val environments: MutableList<Environment> = ArrayList()
        Collections.addAll(environments, *DefaultEnvironment.values())
        LatexDefinitionIndex.getItemsInFileSet(parameters.originalFile).stream()
                .filter { cmd: LatexCommands -> Magic.Command.environmentDefinitions.contains(cmd.name) }
                .map { cmd: LatexCommands -> cmd.requiredParameter(0) }
                .filter { obj: String? -> Objects.nonNull(obj) }
                .map { environmentName: String? -> SimpleEnvironment(environmentName!!) }
                .forEach { e: SimpleEnvironment -> environments.add(e) }

        // Create autocomplete elements.
        result.addAllElements(ContainerUtil.map2List(
                environments
        ) { env: Environment ->
            LookupElementBuilder.create(env, env.environmentName)
                    .withPresentableText(env.environmentName)
                    .bold()
                    .withTailText(env.getArgumentsDisplay() + " " + packageName(env), true)
                    .withIcon(TexifyIcons.DOT_ENVIRONMENT)
        })
        result.addLookupAdvertisement(getKindWords())
    }

    private fun packageName(dependend: Dependend): String {
        val name = dependend.dependency.name
        return if ("" == name) {
            ""
        }
        else " ($name)"
    }

    private fun addCustomCommands(parameters: CompletionParameters, result: CompletionResultSet,
                                  mode: LatexMode? = null) {
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
            val tailText = getTailText(cmd)
            var typeText = getTypeText(cmd)
            val line = 1 + StringUtil.offsetToLineNumber(cmd.containingFile.text, cmd.textOffset)
            typeText = typeText + " " + cmd.containingFile.name + ":" + line
            result.addElement(LookupElementBuilder.create(cmd, cmdName.substring(1))
                    .withPresentableText(cmdName)
                    .bold()
                    .withTailText(tailText, true)
                    .withTypeText(typeText, true)
                    .withInsertHandler(LatexCommandArgumentInsertHandler())
                    .withIcon(TexifyIcons.DOT_COMMAND)
            )
        }
        result.addLookupAdvertisement(getKindWords())
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
                val optional:
                        List<String> = LinkedList(commands.optionalParameters.keys)
                var cmdParameterCount = 0
                if (optional.isNotEmpty()) {
                    try {
                        cmdParameterCount = optional[0].toInt()
                    } catch (ignore: NumberFormatException) {
                    }
                }
                var tailText = Strings.repeat("{param}", min(4, cmdParameterCount))
                if (cmdParameterCount > 4) {
                    tailText = tailText + "... (+" + (cmdParameterCount - 4) + " params)"
                }
                tailText
            }

            "\\DeclarePairedDelimiter" -> "{param}"
            "\\DeclarePairedDelimiterX", "\\DeclarePairedDelimiterXPP" -> {
                val optional = commands.optionalParameters.keys.firstOrNull()
                val nrParams = try {
                    optional?.toInt() ?: 0
                } catch (ignore: java.lang.NumberFormatException) { 0 }
                (1..nrParams).joinToString("") { "{param}" }
            }

            "\\NewDocumentCommand" -> {
                val paramSpecification = commands.requiredParameters[1].removeAll("null", " ")
                paramSpecification.map { c ->
                    if (Magic.Package.xparseParamSpecifiers[c] ?: return "") "{param}"
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