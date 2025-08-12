package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
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

/**
 * Provides context-aware command completions.
 */
object ContextAwareCommandCompletionProvider : LatexContextAwareCompletionAdaptor() {

    override fun addContextAwareCompletions(parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, result: CompletionResultSet) {
        val isClassOrStyleFile = parameters.originalFile.let { it.isClassFile() || it.isStyleFile() }
        val lookupElements = mutableListOf<LookupElementBuilder>()
        for (sd in defBundle.sourcedDefinitions()) {
            if (sd !is SourcedCmdDefinition) continue
            val cmd = sd.entity
            if (!isClassOrStyleFile && cmd.name.contains('@')) {
                // skip internal commands for regular files
                continue
            }
            if (!cmd.isApplicableIn(contexts)) continue // context check
            appendCommandLookupElements(sd, lookupElements, defBundle)
        }
        result.addAllElements(lookupElements)
    }

    private fun buildArgumentInformation(cmd: LSemanticCommand, args: List<LArgument>): String {
        return args.joinToString("")
    }

    private fun buildCommandDisplay(cmd: LSemanticCommand, defBundle: DefinitionBundle): String {
        if (cmd.display == null) {
            return cmd.nameWithSlash
        }
        return cmd.nameWithSlash + " " + cmd.display
    }

    private fun appendCommandLookupElements(sourced: SourcedCmdDefinition, result: MutableCollection<LookupElementBuilder>, defBundle: DefinitionBundle) {
        /*
        The presentation looks like:
        \alpha α                          (amsmath.sty)
        \mycommand[optional]{required}    (main.tex)
         */
        val cmd = sourced.entity
        val default = cmd.dependency == ""
        val typeText = buildCommandSourceStr(sourced) // type text is at the right
        val presentableText = buildCommandDisplay(cmd, defBundle)
        val applicableCtxText = buildApplicableContextStr(cmd)
        cmd.arguments.optionalPowerSet().forEachIndexed { index, subArgs ->
            // Add spaces to the lookup text to distinguish different versions of commands within the same package (optional parameters).
            // Add the package name to the lookup text so we can distinguish between the same commands that come from different packages.
            // This 'extra' text will be automatically inserted by intellij and is removed by the LatexCommandArgumentInsertHandler after insertion.
            val tailText = buildArgumentInformation(cmd, subArgs) + applicableCtxText
            val lookupString = cmd.nameWithSlash + " ".repeat(index + default.not().int) + cmd.dependency
            val l = LookupElementBuilder.create(cmd, lookupString)
                .withPresentableText(presentableText)
                .bold()
                .withTailText(tailText, true)
                .withTypeText(typeText)
                .withInsertHandler(createInsertHandler(cmd, subArgs, defBundle))
                .withIcon(TexifyIcons.DOT_COMMAND)
            result.add(l)
        }
    }

    fun createInsertHandler(semantics: LSemanticCommand, subArgs: List<LArgument>, defBundle: DefinitionBundle): InsertHandler<LookupElement> {
        return NewLatexCommandInsertHandler(semantics, subArgs, defBundle)
    }

    private fun List<LArgument>.optionalPowerSet(): List<List<LArgument>> {
        if (this.isEmpty()) {
            return listOf(emptyList())
        }
        if (this.all { it.isRequired }) {
            return listOf(this)
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