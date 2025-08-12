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

object ContextAwareCompletionProviderDispatcher : LatexContextAwareCompletionAdaptor() {

    private val dispatchMap: Map<LatexContext, LatexContextAwareCompletionProvider> = LatexContexts.run {
        mapOf(
            BibStyle to LatexBibliographyStyleProvider,
            ListType to LatexListTypeProvider,
            MintedFuntimeLand to LatexMintedTypeProvider,
            LabelReference to LatexLabelReferenceProvider,
            CitationKey to LatexBibliographyReferenceProvider,
            LatexContexts.PackageNames to LatexPackageNameProvider,
            LatexContexts.ClassName to LatexDocumentclassProvider,
            GlossaryLabel to LatexGlossariesCompletionProvider,
            ColorReference to LatexXColorProvider,
            LatexContexts.Folder to LatexFolderProvider,
            PicturePath to LatexGraphicsPathProvider,
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