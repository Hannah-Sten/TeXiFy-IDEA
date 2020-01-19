package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.findBibtexItems
import java.util.*

object LatexBibliographyReferenceProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val lookupItems = parameters.originalFile.findBibtexItems()
                .map { bibtexEntry ->
                    val entry = bibtexEntry as BibtexEntry
                    val lookupStrings = LinkedList(entry.authors)
                    lookupStrings.add(entry.title)
                    LookupElementBuilder.create(entry.identifier)
                            .withPsiElement(bibtexEntry)
                            .withPresentableText(entry.title)
                            .bold()
                            .withInsertHandler(LatexReferenceInsertHandler())
                            .withLookupStrings(lookupStrings)
                            .withTypeText(entry.identifier,
                                    true)
                            .withIcon(TexifyIcons.DOT_BIB)

                }
        result.withPrefixMatcher(CamelHumpMatcher(result.prefixMatcher.prefix, false)).addAllElements(lookupItems)
    }


}