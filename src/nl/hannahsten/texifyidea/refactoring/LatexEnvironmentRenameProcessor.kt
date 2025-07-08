package nl.hannahsten.texifyidea.refactoring

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import nl.hannahsten.texifyidea.psi.LatexBeginCommand

/**
 * Rename only the `\begin{}` and `\end{}` commands, not the definition of the environment.
 */
class LatexEnvironmentRenameProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean {
        return element is LatexBeginCommand
    }

    override fun substituteElementToRename(element: PsiElement, editor: Editor?): PsiElement? {
        // If the element is a LatexEnvIdentifier, we want to rename the environment itself, not the definition.
        return if (element is LatexBeginCommand) {
            element.envIdentifier
        }
        else {
            null
        }
    }
}