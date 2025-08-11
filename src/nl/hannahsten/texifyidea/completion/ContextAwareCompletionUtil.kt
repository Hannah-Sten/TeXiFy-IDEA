package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

abstract class LatexContextAwareCompletionProvider : CompletionProvider<CompletionParameters>() {


    protected abstract fun addContextAwareCompletions(
        parameters: CompletionParameters,
        contexts: LContextSet,
        defBundle: DefinitionBundle,
        processingContext: ProcessingContext, result: CompletionResultSet
    )

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val file = parameters.originalFile
        val defBundle = LatexDefinitionService.getInstance(project).getDefBundlesMerged(file)
        val contexts = LatexPsiUtil.resolveContextUpward(parameters.position, defBundle)
        addContextAwareCompletions(parameters, contexts, defBundle, context, result)
        // Add a message to the user that this is an experimental feature.
        result.addLookupAdvertisement("Experimental feature: context-aware completion. ")
    }

    protected fun packageName(entity : LSemanticEntity): String {
        val name = entity.dependency
        return if (name.isEmpty()) "" else " ($name)"
    }
}