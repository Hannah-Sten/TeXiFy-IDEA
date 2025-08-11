package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.SourcedDefinition
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

abstract class LatexContextAwareCompletionProviderBase : CompletionProvider<CompletionParameters>() {

    protected fun getContainingFileName(def : SourcedDefinition) : String? {
        val pointer = def.definitionCommandPointer ?: return null
        val file = pointer.containingFile ?: return null
        return file.name
    }

    protected fun buildCommandSourceStr(sourced: SourcedDefinition): String {
        val cmd = sourced.entity
        return cmd.dependency.ifEmpty {
            getContainingFileName(sourced) ?: "(default)"
        }
    }

    protected abstract fun addContextAwareCompletions(
        parameters: CompletionParameters,
        contexts: LContextSet,
        defBundle: DefinitionBundle,
        processingContext: ProcessingContext, result: CompletionResultSet
    )

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return

        countOfBuilds.incrementAndGet()
        val startTime = System.currentTimeMillis()

        val file = parameters.originalFile
        val defBundle = LatexDefinitionService.getInstance(project).getDefBundlesMerged(file)
        val contexts = LatexPsiUtil.resolveContextUpward(parameters.position, defBundle)
        addContextAwareCompletions(parameters, contexts, defBundle, context, result)
        // Add a message to the user that this is an experimental feature.
        result.addLookupAdvertisement("Experimental feature: context-aware completion. ")

        totalTimeCost.addAndGet(System.currentTimeMillis() - startTime)
    }



    companion object : SimplePerformanceTracker {
        override val countOfBuilds = AtomicInteger(0)
        override val totalTimeCost = AtomicLong(0)
    }
}