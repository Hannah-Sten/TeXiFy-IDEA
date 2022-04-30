package nl.hannahsten.texifyidea.editor.surroundwith

import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class LatexSurroundDescriptor : SurroundDescriptor {

    /**
     * Get the first and last element of the selection, as these are the only
     * elements the [LatexSurrounder] uses to surround a piece of text.
     */
    override fun getElementsToSurround(file: PsiFile?, startOffset: Int, endOffset: Int): Array<PsiElement> {
        val startElement = file?.findElementAt(startOffset) ?: return emptyArray()
        val endElement = file.findElementAt(endOffset - 1) ?: return emptyArray()
        return arrayOf(startElement, endElement)
    }

    override fun isExclusive(): Boolean = false

    override fun getSurrounders(): Array<Surrounder> = arrayOf(
        DoubleQuotesSurrounder(),
        SingleQuotesSurrounder(),
        CustomFoldingRegionSurrounder(),
    )
}