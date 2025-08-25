package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.lang.LContextSet

abstract class LatexContextAgnosticCompletionProvider : CompletionProvider<CompletionParameters>(), LatexContextAwareCompletionProvider {
    abstract fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet)

    final override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        addCompletions(parameters, result)
    }

    final override fun addContextAwareCompletions(
        parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, result: CompletionResultSet
    ) {
        addCompletions(parameters, result)
    }
}