package nl.hannahsten.texifyidea.grammar

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.psi.LatexTypes

/**
 * Lazy loading of token sets to speed up startup, see https://plugins.jetbrains.com/docs/intellij/lexer-and-parser-definition.html#define-a-parser
 */
interface LatexTokenSets {

    companion object {
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(LatexTypes.COMMENT_TOKEN)
        val NORMAL_TEXT = TokenSet.create(LatexTypes.NORMAL_TEXT)
    }
}