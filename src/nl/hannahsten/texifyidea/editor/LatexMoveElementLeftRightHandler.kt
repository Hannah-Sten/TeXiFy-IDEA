package nl.hannahsten.texifyidea.editor

import com.intellij.codeInsight.editorActions.moveLeftRight.MoveElementLeftRightHandler
import com.intellij.psi.PsiElement

class LatexMoveElementLeftRightHandler : MoveElementLeftRightHandler(){

    override fun getMovableSubElements(element: PsiElement): Array<PsiElement> {
        return emptyArray()
    }
}