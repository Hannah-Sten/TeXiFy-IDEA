package nl.hannahsten.texifyidea.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.grammar.LatexLexerAdapter
import nl.hannahsten.texifyidea.psi.LatexTypes

/**
 * Syntax highlighting for lexer tokens (for composite elements, see [LatexAnnotator]).
 *
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = LatexLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<out TextAttributesKey?> = if (tokenType == LatexTypes.OPEN_BRACE || tokenType == LatexTypes.CLOSE_BRACE) {
        BRACES_KEYS
    }
    else if (tokenType == LatexTypes.OPEN_BRACKET || tokenType == LatexTypes.CLOSE_BRACKET) {
        BRACKET_KEYS
    }
    else if (tokenType == LatexTypes.MAGIC_COMMENT_TOKEN) {
        MAGIC_COMMENT_KEYS
    }
    else if (tokenType == LatexTypes.COMMENT_TOKEN) {
        COMMENT_KEYS
    }
    else if (COMMAND_TOKENS.contains(tokenType)) {
        COMMAND_KEYS
    }
    else if (tokenType == LatexTypes.STAR) {
        STAR_KEYS
    }
    else {
        EMPTY_KEYS
    }

    companion object {

        /*
         * TextAttributesKeys
         */
        val BRACES = createKey("LATEX_BRACES", DefaultLanguageHighlighterColors.BRACES)
        val BRACKETS = createKey("LATEX_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val OPTIONAL_PARAM = createKey("LATEX_OPTIONAL_PARAM", DefaultLanguageHighlighterColors.PARAMETER)
        val COMMAND = createKey("LATEX_COMMAND", DefaultLanguageHighlighterColors.KEYWORD)
        val USER_DEFINED_COMMAND = createKey("LATEX_USER_DEFINED_COMMAND", DefaultLanguageHighlighterColors.KEYWORD)
        val COMMAND_MATH_INLINE = createKey("LATEX_COMMAND_MATH_INLINE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
        val COMMAND_MATH_DISPLAY = createKey("LATEX_COMMAND_MATH_DISPLAY", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
        val COMMENT = createKey("LATEX_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val MAGIC_COMMENT = createKey("LATEX_MAGIC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
        val INLINE_MATH = createKey("LATEX_INLINE_MATH", DefaultLanguageHighlighterColors.STRING)
        val DISPLAY_MATH = createKey("LATEX_DISPLAY_MATH", DefaultLanguageHighlighterColors.STRING)
        val STAR = createKey("LATEX_STAR", DefaultLanguageHighlighterColors.DOT)
        val SEPARATOR_EQUALS = createKey("SEPARATOR_EQUALS", DefaultLanguageHighlighterColors.IDENTIFIER)
        val LABEL_DEFINITION = createKey("LATEX_LABEL_DEFINITION", DefaultLanguageHighlighterColors.IDENTIFIER)
        val LABEL_REFERENCE = createKey("LATEX_LABEL_REFERENCE", DefaultLanguageHighlighterColors.IDENTIFIER)
        val BIBLIOGRAPHY_DEFINITION = createKey("LATEX_BIBLIOGRAPHY_DEFINITION", LABEL_DEFINITION)
        val BIBLIOGRAPHY_REFERENCE = createKey("LATEX_BIBLIOGRAPHY_REFERENCE", LABEL_REFERENCE)
        val STYLE_BOLD = createKey("LATEX_STYLE_BOLD", DefaultLanguageHighlighterColors.IDENTIFIER)
        val STYLE_ITALIC = createKey("LATEX_STYLE_ITALIC", DefaultLanguageHighlighterColors.IDENTIFIER)
        val STYLE_UNDERLINE = createKey("LATEX_STYLE_UNDERLINE", DefaultLanguageHighlighterColors.IDENTIFIER)
        val STYLE_STRIKETHROUGH = createKey("LATEX_STYLE_STRIKETHROUGH", DefaultLanguageHighlighterColors.IDENTIFIER)
        val STYLE_SMALL_CAPITALS = createKey("LATEX_STYLE_SMALL_CAPITALS", DefaultLanguageHighlighterColors.IDENTIFIER)
        val STYLE_OVERLINE = createKey("LATEX_STYLE_OVERLINE", DefaultLanguageHighlighterColors.IDENTIFIER)
        val STYLE_TYPEWRITER = createKey("LATEX_STYLE_TYPEWRITER", DefaultLanguageHighlighterColors.IDENTIFIER)
        val STYLE_SLANTED = createKey("LATEX_STYLE_SLANTED", DefaultLanguageHighlighterColors.IDENTIFIER)
        val MATH_NESTED_TEXT = createKey("LATEX_MATH_NESTED_TEXT", DefaultLanguageHighlighterColors.IDENTIFIER)

        private val COMMAND_TOKENS = TokenSet.create(
            LatexTypes.COMMAND_TOKEN,
            LatexTypes.COMMAND_IFNEXTCHAR,
            LatexTypes.BEGIN_TOKEN,
            LatexTypes.END_TOKEN,
            LatexTypes.BEGIN_PSEUDOCODE_BLOCK,
            LatexTypes.MIDDLE_PSEUDOCODE_BLOCK,
            LatexTypes.END_PSEUDOCODE_BLOCK,
            LatexTypes.START_IF,
            LatexTypes.ELSE,
            LatexTypes.END_IF,
            LatexTypes.LEFT,
            LatexTypes.RIGHT,
        )

        /*
         * TextAttributeKey[]s
         */
        private val BRACES_KEYS = keys(BRACES)
        private val BRACKET_KEYS = keys(BRACKETS)
        private val COMMAND_KEYS = keys(COMMAND)
        val USER_DEFINED_COMMAND_KEY = USER_DEFINED_COMMAND
        private val COMMENT_KEYS = keys(COMMENT)
        private val MAGIC_COMMENT_KEYS = keys(MAGIC_COMMENT)
        private val STAR_KEYS = keys(STAR)
        private val EMPTY_KEYS = arrayOfNulls<TextAttributesKey>(0)
        private fun createKey(externalName: String, defaultStyle: TextAttributesKey): TextAttributesKey = TextAttributesKey.createTextAttributesKey(externalName, defaultStyle)

        private fun keys(vararg keys: TextAttributesKey): Array<out TextAttributesKey> = keys
    }
}