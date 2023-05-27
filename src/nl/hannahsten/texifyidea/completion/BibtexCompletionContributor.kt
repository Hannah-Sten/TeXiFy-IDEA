package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import nl.hannahsten.texifyidea.grammar.BibtexLanguage
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexFileProvider
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.FileMagic
import nl.hannahsten.texifyidea.util.parser.*
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class BibtexCompletionContributor : CompletionContributor() {

    init {
        registerTypeCompletion()
        registerKeyCompletion()
        registerStringCompletion()
        registerFileCompletion()
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
                if (type?.text?.lowercase(Locale.getDefault()) == "@string") return@withPattern false

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

    private fun registerFileCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(BibtexContent::class.java)
            .withPattern("File completion pattern") { psiElement, _ ->
                val key = psiElement.firstParentOfType(BibtexTag::class)?.firstChildOfType(BibtexKey::class) ?: return@withPattern false
                // Currently, the bibsource field is used for file sources, but this is apparently not 'official'
                key.text in FileMagic.bibtexFileKeys
            }
            .withLanguage(BibtexLanguage),
        LatexFileProvider()
    )
}
