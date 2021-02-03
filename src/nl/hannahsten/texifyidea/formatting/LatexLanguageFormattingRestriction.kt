package nl.hannahsten.texifyidea.formatting

import com.intellij.lang.LanguageFormattingRestriction
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

/**
 * @author Abby Berkers
 *
 * Disable the formatter on files that start and end with a verbatim environment.
 */
class LatexLanguageFormattingRestriction : LanguageFormattingRestriction {

    override fun isFormatterAllowed(context: PsiElement): Boolean = !isFormatterNotAllowed(context)

    private fun isFormatterNotAllowed(context: PsiElement): Boolean {
        return if (context.containingFile is LatexFile) {
            EnvironmentMagic.verbatim.any {
                context.node.text.startsWith("\\begin{$it}") &&
                    context.node.text.endsWith("\\end{$it}")
            }
        }
        else false
    }
}