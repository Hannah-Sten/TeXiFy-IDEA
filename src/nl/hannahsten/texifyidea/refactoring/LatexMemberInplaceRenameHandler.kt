package nl.hannahsten.texifyidea.refactoring

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.inplace.MemberInplaceRenameHandler

class LatexMemberInplaceRenameHandler : MemberInplaceRenameHandler() {


    override fun isAvailable(element: PsiElement?, editor: Editor, file: PsiFile): Boolean {
        return false
//        element?: return false
//        if(PsiElementRenameHandler.isVetoed(element)) return false
//        val currentElement = file.findElementAt(editor.caretModel.offset)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {

    }

//    private fun getElement(editor: Editor, file: PsiFile) :

}