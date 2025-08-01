package nl.hannahsten.texifyidea.refactoring

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.RenameDialog
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.refactoring.rename.RenamePsiFileProcessor

class LatexFileRenameProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean {
        // The reason below is only applicable for files
        return when (element) {
            is PsiFile -> true
            else -> false
        }
    }

    override fun createRenameDialog(project: Project, element: PsiElement, nameSuggestionContext: PsiElement?, editor: Editor?): RenameDialog {
        // We want to not select the extension in the dialog when renaming files, and looking at RenameDialog#createNewNameComponent(), this is done by setting the editor to null
        return RenamePsiFileProcessor.PsiFileRenameDialog(project, element, nameSuggestionContext, null)
    }
}