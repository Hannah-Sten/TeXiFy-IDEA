package nl.hannahsten.texifyidea.editor.surroundwith

import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class LatexSurroundDescriptor : SurroundDescriptor {
    override fun getElementsToSurround(file: PsiFile?, startOffset: Int, endOffset: Int): Array<PsiElement> {
        // TODO return all PsiElements in range, instead of only the first element.
        return arrayOf(file?.findElementAt(startOffset) ?: return emptyArray())
    }

    override fun isExclusive(): Boolean = false

    override fun getSurrounders(): Array<Surrounder> = arrayOf(
            QuotesSurrounder.DoubleQuotesSurrounder(),
            QuotesSurrounder.SingleQuotesSurrounder()
    )
}