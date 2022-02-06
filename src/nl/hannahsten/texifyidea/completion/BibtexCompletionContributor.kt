package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.hasParent
import nl.hannahsten.texifyidea.util.parentOfType
import nl.hannahsten.texifyidea.util.withPattern
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class BibtexCompletionContributor : CompletionContributor() {

    init {
        registerTypeCompletion()
        registerKeyCompletion()
        registerStringCompletion()
    }

    /**
     * Adds @type functionality to the autocomplete.
     */
    private fun registerTypeCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement(BibtexTypes.TYPE_TOKEN)
            .andNot(PlatformPatterns.psiElement().inside(BibtexEntry::class.java))
            .withLanguage(BibtexLanguage),
        BibtexTypeTokenProvider
    )

    /**
     * Adds key completion to the autocomplete.
     */
    private fun registerKeyCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns
            .psiElement(BibtexTypes.IDENTIFIER)
            .inside(BibtexEntry::class.java)
            .withPattern { psiElement, _ ->
                val entry = psiElement.parentOfType(BibtexEntry::class)
                val type = entry?.firstChildOfType(BibtexType::class)
                if (type?.text?.toLowerCase() == "@string") return@withPattern false

                psiElement.hasParent(BibtexEndtry::class) || psiElement.hasParent(BibtexKey::class)
            }
            .withLanguage(BibtexLanguage),
        BibtexKeyProvider
    )

    /**
     * Adds string support to the autocomplete.
     */
    private fun registerStringCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement(BibtexTypes.IDENTIFIER)
            .inside(BibtexEntry::class.java)
            .inside(BibtexContent::class.java)
            .withLanguage(BibtexLanguage),
        BibtexStringProvider
    )
}
