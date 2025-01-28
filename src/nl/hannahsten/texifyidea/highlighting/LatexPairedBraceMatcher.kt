package nl.hannahsten.texifyidea.highlighting

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.psi.LatexTypes

/**
 * @author Sten Wessel
 */
class LatexPairedBraceMatcher : PairedBraceMatcher {

    private val bracePairs = arrayOf(
        BracePair(LatexTypes.DISPLAY_MATH_START, LatexTypes.DISPLAY_MATH_END, true),
        BracePair(LatexTypes.INLINE_MATH_START, LatexTypes.INLINE_MATH_END, true),
        BracePair(LatexTypes.BEGIN_TOKEN, LatexTypes.END_TOKEN, false),
        BracePair(LatexTypes.OPEN_PAREN, LatexTypes.CLOSE_PAREN, false),
        BracePair(LatexTypes.OPEN_BRACE, LatexTypes.CLOSE_BRACE, false),
        BracePair(LatexTypes.OPEN_BRACKET, LatexTypes.CLOSE_BRACKET, false),
        BracePair(LatexTypes.BEGIN_PSEUDOCODE_BLOCK, LatexTypes.END_PSEUDOCODE_BLOCK, false),
        BracePair(LatexTypes.START_IF, LatexTypes.END_IF, false),
    )

    override fun getPairs() = bracePairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        // Automatic completion by IJ fails with multiple characters for rbrace.
        // Don't insert the other brace when the caret is right before text: in this case we assume the user wants to type the braces separately. This is similar to the Kotlin plugin
        return lbraceType !== LatexTypes.DISPLAY_MATH_START && contextType != LatexTypes.NORMAL_TEXT_WORD
    }

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int) = openingBraceOffset
}
