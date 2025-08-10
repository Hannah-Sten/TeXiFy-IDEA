package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.NewLatexCommandInsertHandler
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.SourcedCmdDefinition
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.util.files.isClassFile
import nl.hannahsten.texifyidea.util.files.isStyleFile
import nl.hannahsten.texifyidea.util.int
import nl.hannahsten.texifyidea.util.repeat


object LatexContextAwareCommandCompletionProvider : LatexContextAwareCompletionProvider() {

    override fun addContextAwareCompletions(parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, processingContext: ProcessingContext, result: CompletionResultSet) {
        val isClassOrStyleFile = parameters.originalFile.let { it.isClassFile() || it.isStyleFile() }
        val lookupElements = mutableListOf<LookupElementBuilder>()
        for (sd in defBundle.sourcedDefinitions()) {
            if (sd !is SourcedCmdDefinition) continue
            val cmd = sd.entity
            if(!isClassOrStyleFile && cmd.name.contains('@')) {
                // skip internal commands for regular files
                continue
            }
            if(!contexts.containsAll(cmd.requiredContext)) continue
            appendCommandLookupElements(cmd, lookupElements)
        }
        result.addAllElements(lookupElements)
    }

    fun createInsertHandler(semantics: LSemanticCommand): InsertHandler<LookupElement> {
        return NewLatexCommandInsertHandler(semantics)
    }


    private fun appendCommandLookupElements(cmd: LSemanticCommand, result: MutableCollection<LookupElementBuilder>) {
        val default = cmd.dependency == ""
        cmd.arguments.optionalPowerSet().forEachIndexed { index, args ->
            // Add spaces to the lookup text to distinguish different versions of commands within the same package (optional parameters).
            // Add the package name to the lookup text so we can distinguish between the same commands that come from different packages.
            // This 'extra' text will be automatically inserted by intellij and is removed by the LatexCommandArgumentInsertHandler after insertion.
            val l = LookupElementBuilder.create(cmd, cmd.nameWithSlash + " ".repeat(index + default.not().int) + cmd.dependency)
                .withPresentableText(cmd.nameWithSlash)
                .bold()
                .withTailText(args.joinToString("") + " " + packageName(cmd), true)
                .withTypeText(cmd.display)
                .withInsertHandler(createInsertHandler(cmd))
                .withIcon(TexifyIcons.DOT_COMMAND)
            result.add(l)
        }
    }

    private fun List<LArgument>.optionalPowerSet(): List<List<LArgument>> {
        if (this.isEmpty()) {
            return listOf(emptyList())
        }
        if (this.all { it.isRequired }) {
            return listOf(this.toList())
        }
        var result = listOf<MutableList<LArgument>>(mutableListOf())
        for (arg in this) {
            if (arg.isRequired) {
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