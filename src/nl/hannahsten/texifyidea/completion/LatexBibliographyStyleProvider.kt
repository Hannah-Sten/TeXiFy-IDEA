package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.MoveToEndOfCommandHandler

/**
 * @author Hannah Schellekens
 */
object LatexBibliographyStyleProvider : CompletionProvider<CompletionParameters>() {

    /**
     * List of all available default bibliography styles.
     */
    private val DEFAULT_STYLES = setOf("abbrv", "acm", "alpha", "apalike", "ieeetr", "plain", "siam", "unsrt")

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        result.addAllElements(ContainerUtil.map2List(DEFAULT_STYLES) { name ->
            LookupElementBuilder.create(name, name)
                    .withPresentableText(name)
                    .bold()
                    .withIcon(TexifyIcons.MISCELLANEOUS_ITEM)
                    .withInsertHandler(MoveToEndOfCommandHandler)
        })
    }
}