package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.rubensten.texifyidea.completion.handlers.TokenTypeInsertHandler

/**
 * @author Ruben Schellekens
 */
object BibtexTypeTokenProvider : CompletionProvider<CompletionParameters>() {

    /**
     * List of all supported entry types in BibTeX.
     */
    private val TOKEN_TYPES = listOf(
            "article", "book", "booklet", "conference", "inbook", "incollection", "inproceedings",
            "manual", "masterthesis", "misc", "phdthesis", "proceedings", "techreport", "unpublished",
            "string", "preamble"
    )

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        result.addAllElements(ContainerUtil.map2List(TOKEN_TYPES, {
            LookupElementBuilder.create(it, it)
                    .withPresentableText(it)
                    .bold()
                    .withIcon(PlatformIcons.ANNOTATION_TYPE_ICON)
                    .withInsertHandler(TokenTypeInsertHandler)
        }))
    }
}