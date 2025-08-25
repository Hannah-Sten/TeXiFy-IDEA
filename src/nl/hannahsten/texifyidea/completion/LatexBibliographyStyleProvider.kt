package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.lang.LContextSet

/**
 * @author Hannah Schellekens
 */
object LatexBibliographyStyleProvider : LatexContextAwareCompletionProvider {

    /**
     * List of all available default bibliography styles.
     */
    private val DEFAULT_STYLES = setOf("abbrv", "acm", "alpha", "apalike", "ieeetr", "plain", "siam", "unsrt")

    override fun addContextAwareCompletions(parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, result: CompletionResultSet) {
        result.addAllElements(
            DEFAULT_STYLES.map { name ->
                LookupElementBuilder.create(name, name)
                    .withPresentableText(name)
                    .bold()
                    .withIcon(TexifyIcons.MISCELLANEOUS_ITEM)
            }
        )
    }
}