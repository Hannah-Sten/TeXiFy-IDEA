package nl.hannahsten.texifyidea.editor

import com.intellij.codeInsight.editorActions.moveLeftRight.MoveElementLeftRightHandler
import com.intellij.psi.PsiElement
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.psi.LatexCommands

class LatexMoveElementLeftRightHandler : MoveElementLeftRightHandler() {

    override fun getMovableSubElements(element: PsiElement): Array<PsiElement> = (element as? LatexCommands)?.parameterList?.toArray(emptyArray()) ?: emptyArray()
}