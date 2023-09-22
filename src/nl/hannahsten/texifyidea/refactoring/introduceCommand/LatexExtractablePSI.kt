package nl.hannahsten.texifyidea.refactoring.introduceCommand

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.startOffset

class LatexExtractablePSI(
    val commonParent: PsiElement,
    val extractableRange: TextRange = TextRange(0, commonParent.textLength)
) : PsiElement by commonParent {
    val extractableTextRange get() = TextRange(commonParent.startOffset + extractableRange.startOffset, commonParent.startOffset + extractableRange.endOffset)
}

