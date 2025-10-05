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
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

interface LatexContextAwareCompletionProvider {

    fun addContextAwareCompletions(
        parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, result: CompletionResultSet
    )
}

abstract class LatexContextAwareCompletionAdaptor : CompletionProvider<CompletionParameters>(), LatexContextAwareCompletionProvider {

    protected fun buildApplicableContextStr(en: LSemanticEntity): String {
        if(en.applicableContext == null) return ""
        return " in ${en.applicableContextDisplay()}"
    }

    protected fun getContainingFileName(def: SourcedDefinition): String? {
        val pointer = def.definitionCommandPointer ?: return null
        val file = pointer.containingFile ?: return null
        return file.name
    }

    protected fun buildCommandSourceStr(sourced: SourcedDefinition): String {
        val cmd = sourced.entity
        val dependency = cmd.dependency
        if (dependency.isCustom) {
            // If the command is defined in the current file, we can use the file name.
            return getContainingFileName(sourced) ?: "(unknown)"
        }
        return dependency.name
    }

    abstract override fun addContextAwareCompletions(
        parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, result: CompletionResultSet
    )

    final override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return

        performanceTracker.track {
            val file = parameters.originalFile
            val defBundle = LatexDefinitionService.getInstance(project).getDefBundlesMerged(file)
            val latexContexts = LatexPsiUtil.resolveContextUpward(parameters.position, defBundle)
            addContextAwareCompletions(parameters, latexContexts, defBundle, result)
            // Add a message to the user that this is an experimental feature.
            result.addLookupAdvertisement("Experimental feature: context-aware completion. ")
        }
    }

    companion object {
        val performanceTracker = SimplePerformanceTracker()
    }
}