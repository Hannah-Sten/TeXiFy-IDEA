package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexCommandArgumentInsertHandler
import nl.hannahsten.texifyidea.completion.handlers.LatexCommandInsertHandler
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.lang.Dependend
import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexMathCommand
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Kindness.getKindWords
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.files.isClassFile
import nl.hannahsten.texifyidea.util.files.isStyleFile
import nl.hannahsten.texifyidea.util.int
import nl.hannahsten.texifyidea.util.repeat

abstract class LatexCommandCompletionProviderBase : CompletionProvider<CompletionParameters>() {

    /**
     * Add any commands that were not found in the indexed commands but are hardcoded in LatexRegularCommand.
     * If the index was not yet ready, add all of them.
     */
    protected fun addPredefinedCommands(
        result: CompletionResultSet, file: PsiFile, project: Project,
        defaultCommands: Set<LatexCommand>,
        lookupFromPackage: Map<String, Set<LatexCommand>>
    ) {
        // These are bound to be added
        val lookupElements = mutableListOf<LookupElementBuilder>()
        defaultCommands.forEach { appendCommandLookupElements(it, lookupElements) }
        if (!DumbService.isDumb(project)) {
            // let us search for the indexed commands
            val packages = PackageUtils.getIncludedPackagesInFileset(file).toSet()
            packages.forEach { packageName ->
                lookupFromPackage[packageName]?.forEach {
                    appendCommandLookupElements(it, lookupElements)
                }
            }
        }
        result.addAllElements(lookupElements)
        result.addLookupAdvertisement(getKindWords())
    }

    protected fun addStubIndexCustomCommands(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
        project: Project,
        filesetScope: GlobalSearchScope,
        definitionCommands: Collection<LatexCommands>
    ) {
        val file = parameters.originalFile
        val isClassOrStyleFile = file.isClassFile() || file.isStyleFile()
        val lookupElements = mutableListOf<LookupElementBuilder>()
        for (defCmd in definitionCommands) {
            val cmd = LatexPsiUtil.getDefinedCommandName(defCmd) ?: continue
            if (!isClassOrStyleFile) {
                if (cmd.contains('@')) {
                    // skip internal commands for regular files
                    continue
                }
            }
            val lookupElement =
                LookupElementBuilder.create(cmd, cmd)
                    .withPresentableText(cmd)
                    .bold()
                    .withInsertHandler(LatexCommandArgumentInsertHandler(emptyList()))
                    .withIcon(TexifyIcons.DOT_COMMAND)
            lookupElements.add(lookupElement)
        }
        result.addAllElements(lookupElements)
    }

    fun createInsertHandler(args: List<Argument>): InsertHandler<LookupElement> {
        return LatexCommandInsertHandler(args)
    }

    protected fun packageName(dependend: Dependend): String {
        val name = dependend.dependency.name
        return if (name.isEmpty()) "" else " ($name)"
    }

    protected fun appendCommandLookupElements(cmd: LatexCommand, result: MutableCollection<LookupElementBuilder>) {
        cmd.arguments.optionalPowerSet().forEachIndexed { index, args ->
            // Add spaces to the lookup text to distinguish different versions of commands within the same package (optional parameters).
            // Add the package name to the lookup text so we can distinguish between the same commands that come from different packages.
            // This 'extra' text will be automatically inserted by intellij and is removed by the LatexCommandArgumentInsertHandler after insertion.
            val default = cmd.dependency.isDefault
            val l = LookupElementBuilder.create(cmd, cmd.commandWithSlash + " ".repeat(index + default.not().int) + cmd.dependency.displayString)
                .withPresentableText(cmd.commandWithSlash)
                .bold()
                .withTailText(args.joinToString("") + " " + packageName(cmd), true)
                .withTypeText(cmd.display)
                .withInsertHandler(createInsertHandler(args))
                .withIcon(TexifyIcons.DOT_COMMAND)
            result.add(l)
        }
    }

    companion object {

        private fun Array<out Argument>.optionalPowerSet(): List<List<Argument>> {
            if (this.isEmpty()) {
                return listOf(emptyList())
            }
            if (this.all { it is RequiredArgument }) {
                return listOf(this.toList())
            }
            var result = listOf<MutableList<Argument>>(mutableListOf())
            for (arg in this) {
                if (arg is RequiredArgument) {
                    result.forEach { it.add(arg) }
                }
                else {
                    val noAdd = result.map { it.toMutableList() }
                    result.forEach { it.add(arg) }
                    result = result + noAdd
                }
            }
            return result
        }
    }
}

object LatexNormalCommandCompletionProvider : LatexCommandCompletionProviderBase() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val filesetScope = LatexProjectStructure.getFilesetScopeFor(parameters.originalFile)
        addPredefinedCommands(
            result, parameters.originalFile, project,
            LatexRegularCommand.defaultCommands, LatexRegularCommand.lookupFromPackage
        )
        addStubIndexCustomCommands(
            parameters, context, result, project, filesetScope,
            NewSpecialCommandsIndex.getAllRegularCommandDef(project, filesetScope)
        )
        result.addLookupAdvertisement(getKindWords())
    }
}

object LatexMathCommandCompletionProvider : LatexCommandCompletionProviderBase() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val filesetScope = LatexProjectStructure.getFilesetScopeFor(parameters.originalFile)
        addPredefinedCommands(
            result, parameters.originalFile, project,
            LatexMathCommand.defaultCommands, LatexMathCommand.lookupFromPackage
        )
        addStubIndexCustomCommands(
            parameters, context, result, project, filesetScope,
            NewSpecialCommandsIndex.getAllCommandDef(project, filesetScope)
        )

        // This lookup advertisement is added only for math commands
        result.addLookupAdvertisement("Don't use \\\\ outside of tabular or math mode, it's evil.")
    }
}