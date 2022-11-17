package nl.hannahsten.texifyidea.grammar

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.psi.BibtexTypes

/**
 * See [LatexTokenSets].
 */
interface BibtexTokenSets {

    companion object {

        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(BibtexTypes.COMMENT)
        val NORMAL_TEXT = TokenSet.create(BibtexTypes.NORMAL_TEXT)
    }
}