package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import nl.rubensten.texifyidea.BibtexLanguage
import nl.rubensten.texifyidea.psi.BibtexEntry
import nl.rubensten.texifyidea.psi.BibtexTypes

/**
 * @author Ruben Schellekens
 */
open class BibtexCompletionContributor : CompletionContributor() {

    init {
        // Outer scope: types.
        extend(
                CompletionType.BASIC,
                psiElement(BibtexTypes.TYPE_TOKEN)
                        .andNot(psiElement().inside(BibtexEntry::class.java))
                        .withLanguage(BibtexLanguage),
                BibtexTypeTokenProvider
        )
    }
}