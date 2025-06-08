package nl.hannahsten.texifyidea.formatting

import com.intellij.lang.LanguageFormattingRestriction
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

/**
 * Disable the formatter for verbatim environments.
 *
 * @author Abby Berkers
 *
 */
class LatexLanguageFormattingRestriction : LanguageFormattingRestriction {

    override fun isFormatterAllowed(context: PsiElement): Boolean = !isFormatterNotAllowed(context)

    private fun isFormatterNotAllowed(context: PsiElement): Boolean {
        if(context !is LatexEnvironment) return false
        return context.getEnvironmentName() in EnvironmentMagic.verbatim
    }
}