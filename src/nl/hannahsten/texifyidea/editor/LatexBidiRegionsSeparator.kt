package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.editor.bidi.TokenSetBidiRegionsSeparator
import com.intellij.psi.tree.IElementType

/**
 * Considers all tokens united. Let's the editor handle bidirectional text
 */
class LatexBidiRegionsSeparator : TokenSetBidiRegionsSeparator(null) {
    override fun createBorderBetweenTokens(previousTokenType: IElementType, tokenType: IElementType): Boolean {
        return false
    }
}