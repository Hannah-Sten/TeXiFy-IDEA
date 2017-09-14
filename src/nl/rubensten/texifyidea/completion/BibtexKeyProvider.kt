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
import nl.rubensten.texifyidea.psi.BibtexEntry
import nl.rubensten.texifyidea.util.parentOfType
import nl.rubensten.texifyidea.util.tokenType

/**
 * @author Ruben Schellekens
 */
object BibtexKeyProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet) {
        val psiElement = parameters.position
        val entry = psiElement.parentOfType(BibtexEntry::class) ?: return
        val token = entry.tokenType() ?: return
        val entryType = BibtexDefaultEntry[token] ?: return
        val optional = entryType.optional.toSet()
        val required = entryType.required.toSet()

        result.addAllElements(ContainerUtil.map2List(entryType.allFields(), {
            val message = when (it) {
                in required -> "required"
                in optional -> "optional"
                else -> ""
            }

            LookupElementBuilder.create(it, it.fieldName)
                    .withPresentableText(it.fieldName)
                    .bold()
                    .withTailText(message, true)
                    .withIcon(PlatformIcons.PROTECTED_ICON)
                    .withInsertHandler(TokenTypeInsertHandler)
        }))
    }
}