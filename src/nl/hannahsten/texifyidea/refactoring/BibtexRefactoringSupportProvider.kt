package nl.hannahsten.texifyidea.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.BibtexId

/**
 * This class is used to enable inline refactoring.
 */
class BibtexRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return element is BibtexId
    }
}