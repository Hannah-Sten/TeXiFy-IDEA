package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContext
import nl.hannahsten.texifyidea.lang.LatexContexts

object ContextAwareCompletionProviderDispatcher : LatexContextAwareCompletionAdaptor() {

    private val contextToCompletionProviderList:
        List<Pair<LatexContext, LatexContextAwareCompletionProvider>> = LatexContexts.run {
            listOf(
                BibStyle to LatexBibliographyStyleProvider,
                ListType to LatexListTypeProvider,
                MintedFuntimeLand to LatexMintedTypeProvider,
                LabelReference to LatexLabelReferenceProvider,
                CitationKey to LatexBibliographyReferenceProvider,
                LatexContexts.PackageNames to LatexPackageNameProvider,
                GlossaryLabel to LatexGlossariesCompletionProvider,
                LatexContexts.ClassName to LatexDocumentclassProvider,
                ColorReference to LatexXColorProvider
            )
        }

    private val contextToCompletionProvider: Map<LatexContext, MutableList<LatexContextAwareCompletionProvider>> = buildMap {
        contextToCompletionProviderList.forEach { (context, provider) ->
            val list = getOrPut(context) { mutableListOf() }
            list.add(provider)
        }
    }

    override fun addContextAwareCompletions(
        parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle,
        result: CompletionResultSet
    ) {
        contexts.forEach {
            contextToCompletionProvider[it]?.forEach { provider ->
                provider.addContextAwareCompletions(parameters, contexts, defBundle, result)
            }
        }
    }
}