package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.PlatformIcons
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.completion.handlers.TokenTypeInsertHandler
import nl.rubensten.texifyidea.lang.BibtexDefaultEntry
import nl.rubensten.texifyidea.psi.BibtexEntry
import nl.rubensten.texifyidea.util.and
import nl.rubensten.texifyidea.util.keyNames
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
        val optional = entryType.optional.map { it.fieldName }.toSet()
        val required = entryType.required.map { it.fieldName }.toSet()

        // Removed already present items.
        val fields = entryType.allFields().map { it.fieldName }
        val keys = entry.keyNames()
        val notPresent = fields - keys

        // Add lookup elements.
        result.addAllElements(ContainerUtil.map2List(notPresent, {
            val (message, icon) = when (it) {
                in required -> " required" and TexifyIcons.KEY_REQUIRED
                in optional -> " optional" and PlatformIcons.PROTECTED_ICON
                else -> "" and PlatformIcons.PROTECTED_ICON
            }

            LookupElementBuilder.create(it, it)
                    .withPresentableText(it)
                    .bold()
                    .withTypeText(message, true)
                    .withIcon(icon)
                    .withInsertHandler(TokenTypeInsertHandler)
        }))
    }
}