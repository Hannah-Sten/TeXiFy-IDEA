package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Hannah Schellekens
 */
object LatexListTypeProvider : CompletionProvider<CompletionParameters>() {

    /**
     * List of all available default list types.
     */
    private val DEFAULT_LIST_TYPES = setOf("itemize", "enumerate", "description")

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        result.addAllElements(
            ContainerUtil.map2List(DEFAULT_LIST_TYPES) { name ->
                LookupElementBuilder.create(name, name)
                    .withPresentableText(name)
                    .bold()
                    .withIcon(TexifyIcons.MISCELLANEOUS_ITEM)
            }
        )
    }
}