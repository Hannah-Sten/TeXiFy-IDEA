package nl.hannahsten.texifyidea.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexParameterText

/**
 * This class is used to enable inline refactoring.
 */
class LatexRefactoringSupportProvider : RefactoringSupportProvider() {

    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        // Label parameters are LatexParameterText
        return when (element) {
            is LatexParameterText -> true
            else -> false
        }
    }

    override fun isSafeDeleteAvailable(element: PsiElement): Boolean {
        return element is LatexParameterText
    }
}