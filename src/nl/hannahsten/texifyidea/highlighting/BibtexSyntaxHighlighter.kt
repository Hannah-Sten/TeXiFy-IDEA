package nl.hannahsten.texifyidea.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.BibtexLexerAdapter
import nl.hannahsten.texifyidea.psi.BibtexTypes

/**
 * @author Hannah Schellekens
 */
open class BibtexSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {

        val ASSIGNMENT = createTextAttributesKey("BIBTEX_ASSIGNMENT", OPERATION_SIGN)
        val BRACES = createTextAttributesKey("BIBTEX_BRACES", LatexSyntaxHighlighter.BRACES)
        val COMMENTS = createTextAttributesKey("BIBTEX_COMMENTS", LatexSyntaxHighlighter.COMMENT)
        val CONCATENATION = createTextAttributesKey("BIBTEX_CONCATENATION", OPERATION_SIGN)
        val IDENTIFIER = createTextAttributesKey("BIBTEX_IDENTIFIER", INSTANCE_FIELD)
        val KEY = createTextAttributesKey("BIBTEX_KEY", PARAMETER)
        val NUMBER = createTextAttributesKey("BIBTEX_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val STRING = createTextAttributesKey("BIBTEX_STRING", DefaultLanguageHighlighterColors.STRING)
        val TYPE_TOKEN = createTextAttributesKey("BIBTEX_TYPE_TOKEN", KEYWORD)
        val VALUE = createTextAttributesKey("BIBTEX_VALUE", DefaultLanguageHighlighterColors.IDENTIFIER)

        val ASSIGNMENT_KEYS = arrayOf(ASSIGNMENT)
        val BRACES_KEYS = arrayOf(BRACES)
        val COMMENT_KEYS = arrayOf(COMMENTS)
        val CONCATENATION_KEYS = arrayOf(CONCATENATION)
        val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        val KEY_KEYS = arrayOf(KEY)
        val NUMBER_KEYS = arrayOf(NUMBER)
        val STRING_KEYS = arrayOf(STRING)
        val TYPE_TOKEN_KEYS = arrayOf(TYPE_TOKEN)
        val VALUE_KEYS = arrayOf(VALUE)
        val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer() = BibtexLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType?) = when (tokenType) {
        BibtexTypes.ASSIGNMENT -> ASSIGNMENT_KEYS
        BibtexTypes.OPEN_BRACE, BibtexTypes.CLOSE_BRACE, BibtexTypes.OPEN_PARENTHESIS -> BRACES_KEYS
        BibtexTypes.COMMENT, BibtexTypes.COMMENT_TOKEN -> COMMENT_KEYS
        BibtexTypes.CONCATENATE -> CONCATENATION_KEYS
        BibtexTypes.IDENTIFIER, BibtexTypes.VERBATIM_IDENTIFIER -> IDENTIFIER_KEYS
        BibtexTypes.KEY -> KEY_KEYS
        BibtexTypes.NUMBER -> NUMBER_KEYS
        BibtexTypes.TYPE_TOKEN -> TYPE_TOKEN_KEYS
        BibtexTypes.STRING, BibtexTypes.QUOTED_STRING, BibtexTypes.QUOTED_VERBATIM -> STRING_KEYS
        BibtexTypes.CONTENT, BibtexTypes.BRACED_STRING, BibtexTypes.BRACED_VERBATIM -> VALUE_KEYS
        else -> EMPTY_KEYS
    }
}