package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Hannah Schellekens
 */
object LatexListTypeProvider : LatexContextAgnosticCompletionProvider() {

    /**
     * List of all available default list types.
     */
    private val DEFAULT_LIST_TYPES = setOf("itemize", "enumerate", "description")

    override fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        result.addAllElements(
            DEFAULT_LIST_TYPES.map { name ->
                LookupElementBuilder.create(name, name)
                    .withPresentableText(name)
                    .bold()
                    .withIcon(TexifyIcons.MISCELLANEOUS_ITEM)
            }
        )
    }
}