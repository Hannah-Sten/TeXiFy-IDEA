package nl.rubensten.texifyidea.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import nl.rubensten.texifyidea.LatexLexerAdapter;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class LatexSyntaxHighlighter extends SyntaxHighlighterBase {

    /*
     * TextAttributesKeys
     */
    public static final TextAttributesKey BRACES = createKey("LATEX_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKETS = createKey("LATEX_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey OPTIONAL_PARAM = createKey("LATEX_OPTIONAL_PARAM", DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey COMMAND = createKey("LATEX_COMMAND", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey COMMAND_MATH_INLINE = createKey("LATEX_COMMAND_MATH_INLINE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey COMMAND_MATH_DISPLAY = createKey("LATEX_COMMAND_MATH_DISPLAY", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey COMMENT = createKey("LATEX_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey INLINE_MATH = createKey("LATEX_INLINE_MATH", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey DISPLAY_MATH = createKey("LATEX_DISPLAY_MATH", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey STAR = createKey("LATEX_STAR", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey LABEL_DEFINITION = createKey("LATEX_LABEL_DEFINITION", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey LABEL_REFERENCE = createKey("LATEX_LABEL_REFERENCE", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey BIBLIOGRAPHY_DEFINITION = createKey("LATEX_BIBLIOGRAPHY_DEFINITION", LABEL_DEFINITION);
    public static final TextAttributesKey BIBLIOGRAPHY_REFERENCE = createKey("LATEX_BIBLIOGRAPHY_REFERENCE", LABEL_REFERENCE);
    public static final TextAttributesKey STYLE_BOLD = createKey("LATEX_STYLE_BOLD", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey STYLE_ITALIC = createKey("LATEX_STYLE_ITALIC", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey STYLE_UNDERLINE = createKey("LATEX_STYLE_UNDERLINE", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey STYLE_STRIKETHROUGH = createKey("LATEX_STYLE_STRIKETHROUGH", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey STYLE_SMALL_CAPITALS = createKey("LATEX_STYLE_SMALL_CAPITALS", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey STYLE_OVERLINE = createKey("LATEX_STYLE_OVERLINE", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey STYLE_TYPEWRITER = createKey("LATEX_STYLE_TYPEWRITER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey STYLE_SLANTED = createKey("LATEX_STYLE_SLANTED", DefaultLanguageHighlighterColors.IDENTIFIER);

    private static final TokenSet COMMAND_TOKENS = TokenSet.create(
            LatexTypes.COMMAND_TOKEN,
            LatexTypes.BEGIN_TOKEN,
            LatexTypes.END_TOKEN
    );

    /*
     * TextAttributeKey[]s
     */
    private static final TextAttributesKey[] BRACES_KEYS = keys(BRACES);
    private static final TextAttributesKey[] BRACKET_KEYS = keys(BRACKETS);
    private static final TextAttributesKey[] COMMAND_KEYS = keys(COMMAND);
    private static final TextAttributesKey[] COMMENT_KEYS = keys(COMMENT);
    private static final TextAttributesKey[] INLINE_MATH_KEYS = keys(INLINE_MATH);
    private static final TextAttributesKey[] DISPLAY_MATH_KEYS = keys(DISPLAY_MATH);
    private static final TextAttributesKey[] STAR_KEYS = keys(STAR);
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    private static TextAttributesKey createKey(String externalName, TextAttributesKey defaultStyle) {
        return TextAttributesKey.createTextAttributesKey(externalName, defaultStyle);
    }

    private static TextAttributesKey[] keys(TextAttributesKey... keys) {
        return keys;
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new LatexLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        // Braces
        if (tokenType.equals(LatexTypes.OPEN_BRACE) ||
                tokenType.equals(LatexTypes.CLOSE_BRACE)) {
            return BRACES_KEYS;
        }
        // Brackets
        else if (tokenType.equals(LatexTypes.OPEN_BRACKET) ||
                tokenType.equals(LatexTypes.CLOSE_BRACKET)) {
            return BRACKET_KEYS;
        }
        // Comments
        else if (tokenType.equals(LatexTypes.COMMENT_TOKEN)) {
            return COMMENT_KEYS;
        }
        // Commands
        else if (COMMAND_TOKENS.contains(tokenType)) {
            return COMMAND_KEYS;
        }
        // Math environment
        else if (tokenType.equals(LatexTypes.INLINE_MATH_END)) {
            return INLINE_MATH_KEYS;
        }
        else if (tokenType.equals(LatexTypes.DISPLAY_MATH_START) ||
                tokenType.equals(LatexTypes.DISPLAY_MATH_END)) {
            return DISPLAY_MATH_KEYS;
        }
        // Star
        else if (tokenType.equals(LatexTypes.STAR)) {
            return STAR_KEYS;
        }
        // When no supported highlights is available
        else {
            return EMPTY_KEYS;
        }
    }
}
