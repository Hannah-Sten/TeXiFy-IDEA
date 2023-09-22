package nl.hannahsten.texifyidea.refactoring.introduceCommand

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class LatexExtractablePSI(
    val commonParent: PsiElement,
    val extractableRange: TextRange = TextRange(0, commonParent.textLength)
) : PsiElement by commonParent {

}

