package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.util.Magic

object LatexXColorProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        addDefaultColors(result)
        addCustomColors(result)
    }

    private fun addDefaultColors(result: CompletionResultSet) {
        result.addAllElements(
                Magic.Colors.defaultXcolors.map {
                    LookupElementBuilder.create(it)
                }
        )
    }

    private fun addCustomColors(result: CompletionResultSet) {
    }
}