package nl.hannahsten.texifyidea.psi

import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import nl.hannahsten.texifyidea.lang.LatexAnnotation
import nl.hannahsten.texifyidea.util.annotations

/**
 *
 * @author Sten Wessel
 */
class LatexLanguageInjector : LanguageInjector {
    override fun getLanguagesToInject(host: PsiLanguageInjectionHost, registrar: InjectedLanguagePlaces) {
        if (host is LatexEnvironment) {
            val languageId = host.annotations().lastOrNull { it.key == LatexAnnotation.KEY_INJECT_LANGUAGE }?.value ?: return
            val language = Language.findLanguageByID(languageId) ?: return

            val range = host.environmentContent?.textRange?.shiftRight(-host.textOffset) ?: TextRange.EMPTY_RANGE

            registrar.addPlace(language, range, null, null)
        }
    }
}