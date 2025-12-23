package nl.hannahsten.texifyidea.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.BibtexId

/**
 * This class is used to enable inline refactoring.
 */
class BibtexRefactoringSupportProvider : RefactoringSupportProvider() {

    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        // Inline refactoring is not enabled for BibtexId because it will not handle the comma separator correctly when the bibtex id is renamed directly (not from usages).
        return false
    }

    override fun isSafeDeleteAvailable(element: PsiElement): Boolean = element is BibtexId
}