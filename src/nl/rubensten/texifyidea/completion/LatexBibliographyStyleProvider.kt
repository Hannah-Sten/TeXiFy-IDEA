package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil

/**
 * @author Ruben Schellekens
 */
object LatexBibliographyStyleProvider : CompletionProvider<CompletionParameters>() {

    /**
     * List of all available default bibliography styles.
     */
    private val DEFAULT_STYLES = setOf("abbrv", "acm", "alpha", "apalike", "ieeetr", "plain", "siam", "unsrt")

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        result.addAllElements(ContainerUtil.map2List(DEFAULT_STYLES) { name ->
            LookupElementBuilder.create(name, name)
                    .withPresentableText(name)
                    .bold()
                    .withIcon(PlatformIcons.PACKAGE_LOCAL_ICON)
                    .withInsertHandler(BibliographyStyleInsertHandler)
        })
    }

    /**
     * Makes the caret skip over the last brace.
     *
     * @author Ruben Schellekens
     */
    private object BibliographyStyleInsertHandler : InsertHandler<LookupElement> {

        override fun handleInsert(context: InsertionContext?, item: LookupElement?) {
            val editor = context?.editor ?: return
            val caret = editor.caretModel
            caret.moveToOffset(caret.offset + 1)
        }
    }
}