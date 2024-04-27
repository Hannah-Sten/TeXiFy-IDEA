package nl.hannahsten.texifyidea.index

import com.intellij.lexer.Lexer
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.search.IndexPatternBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.grammar.LatexTokenSets

class LatexIndexPatternBuilder : IndexPatternBuilder {
    override fun getIndexingLexer(file: PsiFile): Lexer? {
        return if (file is LatexFile) {
            LatexParserDefinition().createLexer(file.project)
        }
        else {
            null
        }
    }

    override fun getCommentTokenSet(file: PsiFile): TokenSet = LatexTokenSets.COMMENTS

    override fun getCommentStartDelta(tokenType: IElementType?): Int {
        return 0
    }

    override fun getCommentEndDelta(tokenType: IElementType?): Int {
        return 0
    }

    override fun getCharsAllowedInContinuationPrefix(tokenType: IElementType): String {
        return "%"
    }
}