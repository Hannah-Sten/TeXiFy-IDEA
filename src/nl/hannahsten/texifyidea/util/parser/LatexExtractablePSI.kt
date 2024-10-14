package nl.hannahsten.texifyidea.util.parser

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.util.toIntRange

/**
 * A wrapper for [PsiElement] that includes a range to extract. This allows extracting subtext from a text block or other non-total extracitons
 */
class LatexExtractablePSI(
    val self: PsiElement,
    /**
     * The range of text to extract relative to the start of my text
     */
    val extractableRange: TextRange = TextRange(0, self.textLength)
) : PsiElement by self {
    /**
     * The range of text to extract relative to the file's start.
     */
    val extractableRangeInFile
        get() = TextRange(
            startOffset + extractableRange.startOffset,
            startOffset + extractableRange.endOffset
        )

    val extractableIntRange
        get() = extractableRange.toIntRange()
}

fun PsiElement.asExtractable(): LatexExtractablePSI = LatexExtractablePSI(this)

fun PsiElement.asExtractable(range: TextRange): LatexExtractablePSI = LatexExtractablePSI(this, range)