package nl.hannahsten.texifyidea.psi

import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.magicComment

/**
 * Inject language based on magic comments.
 *
 * @author Sten Wessel
 */
class LatexLanguageInjector : LanguageInjector {
    override fun getLanguagesToInject(host: PsiLanguageInjectionHost, registrar: InjectedLanguagePlaces) {
        if (host is LatexEnvironment) {
            val magicComment = host.magicComment()
            if (!magicComment.containsKey(DefaultMagicKeys.INJECT_LANGUAGE)) return
            val languageId = magicComment.value(DefaultMagicKeys.INJECT_LANGUAGE)
            val language = Language.findLanguageByID(languageId) ?: return

            val range = host.environmentContent?.textRange?.shiftRight(-host.textOffset) ?: TextRange.EMPTY_RANGE

            registrar.addPlace(language, range, null, null)
        }
    }
}