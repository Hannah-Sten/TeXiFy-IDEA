package nl.hannahsten.texifyidea.refactoring

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.RefactoringActionHandler
import nl.hannahsten.texifyidea.psi.LatexEnvIdentifier
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.refactoring.introducecommand.LatexExtractCommandHandler

/**
 * This class is used to enable inline refactoring.
 */
class LatexRefactoringSupportProvider : RefactoringSupportProvider() {

    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        // Label parameters are LatexParameterText
        return when (element) {
            is LatexParameterText -> true
            is LatexEnvIdentifier -> true
            else -> false
        }
    }

    override fun isSafeDeleteAvailable(element: PsiElement): Boolean = element is LatexParameterText

    override fun getIntroduceVariableHandler(): RefactoringActionHandler = LatexExtractCommandHandler()
}