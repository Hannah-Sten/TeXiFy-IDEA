package nl.hannahsten.texifyidea.refactoring

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.inplace.MemberInplaceRenameHandler
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.psi.LatexEnvIdentifier
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

/**
 * Rename only the environment identifier of a begin or end command, but not in the definition.
 */
class LatexEnvironmentRenameHandler : MemberInplaceRenameHandler() {

    override fun isAvailable(element: PsiElement?, editor: Editor, file: PsiFile): Boolean {
        return getElement(editor, file) != null
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext) {
        val element = getElement(editor, file) ?: return
        if (checkAvailable(element, editor, dataContext)) {
            doRename(element, editor, dataContext)
        }
    }

    private fun getElement(editor: Editor, file: PsiFile): PsiElement? {
        if(file !is LatexFile) return null
        val currentElement = file.findElementAt(editor.caretModel.offset) ?: return null
        val envIdentifier = currentElement.firstParentOfType<LatexEnvIdentifier>(1) ?: return null
        return envIdentifier
    }
}