package nl.hannahsten.texifyidea.refactoring

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import com.intellij.refactoring.rename.inplace.MemberInplaceRenameHandler
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.LatexEndCommand
import nl.hannahsten.texifyidea.psi.LatexEnvIdentifier
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.firstParent
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

class LatexMemberInplaceRenameHandler : MemberInplaceRenameHandler() {


    override fun isAvailable(element: PsiElement?, editor: Editor, file: PsiFile): Boolean {
        val curElement = getElement(editor, file) ?: return false
        return true
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext) {
        val element = getElement(editor,file)
        if(element == null) return
        if (checkAvailable(element, editor, dataContext)) {
            doRename(element, editor, dataContext)
        }
    }

    private fun getElement(editor: Editor, file: PsiFile) : PsiElement?{
        if(file !is LatexFile) return null
        val currentElement = file.findElementAt(editor.caretModel.offset) ?: return null
        val envIdentifier = currentElement.firstParentOfType<LatexEnvIdentifier>(1) ?: return null
//        if(currentElement !is LatexEnvIdentifier) return null
        return envIdentifier
//        val beginEnd = currentElement.firstParent(3) {
//            it is LatexBeginCommand || it is LatexEndCommand
//        } ?: return null
//
//        return beginEnd.findFirstChildTyped<LatexEnvIdentifier>()
//        if(currentElement.firstParentOfType<LatexBeginCommand>(3) == null &&
//            currentElement.firstParentOfType<LatexEndCommand>(3) == null
//        ) return null
//        return currentElement
    }



}