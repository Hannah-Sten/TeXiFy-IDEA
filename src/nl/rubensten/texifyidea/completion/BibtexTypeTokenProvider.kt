package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.rubensten.texifyidea.completion.handlers.TokenTypeInsertHandler
import nl.rubensten.texifyidea.lang.BibtexDefaultEntry

/**
 * @author Ruben Schellekens
 */
object BibtexTypeTokenProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        result.addAllElements(ContainerUtil.map2List(BibtexDefaultEntry.values(), {
            LookupElementBuilder.create(it, it.token)
                    .withPresentableText(it.token)
                    .bold()
                    .withIcon(PlatformIcons.ANNOTATION_TYPE_ICON)
                    .withInsertHandler(TokenTypeInsertHandler)
        }))
    }
}