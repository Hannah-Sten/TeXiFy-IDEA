package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexFileProvider
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexFolderProvider
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContext
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.SimpleFileInputContext

/**
 * Defines a dispatcher for context-aware completion providers.
 *
 * @see LatexContexts
 */
object ContextAwareCompletionProviderDispatcher : LatexContextAwareCompletionAdaptor() {

    private val dispatchMap: Map<LatexContext, LatexContextAwareCompletionProvider> = LatexContexts.run {
        mapOf(
            ClassName to LatexDocumentclassProvider,
            PackageNames to LatexPackageNameProvider,
            Folder to LatexFolderProvider,
            BibStyle to LatexBibliographyStyleProvider,
            LabelReference to LatexLabelReferenceProvider,
            CitationKey to LatexBibliographyReferenceProvider,
            PicturePath to LatexGraphicsPathProvider,
            GlossaryLabel to LatexGlossariesCompletionProvider,
            ListType to LatexListTypeProvider,
            ColorReference to LatexXColorProvider,
            MintedFuntimeLand to LatexMintedTypeProvider,
        )
    }

    private fun additionalDispatching(context: LatexContext): LatexContextAwareCompletionProvider? {
        if (context is SimpleFileInputContext) {
            return LatexFileProvider
        }
        return null
    }

    private fun dispatchContext(context: LatexContext): LatexContextAwareCompletionProvider? {
        return dispatchMap[context] ?: additionalDispatching(context)
    }

    override fun addContextAwareCompletions(
        parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle,
        result: CompletionResultSet
    ) {
        contexts.forEach {
            dispatchContext(it)?.addContextAwareCompletions(parameters, contexts, defBundle, result)
        }
    }
}