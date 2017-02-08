package nl.rubensten.texifyidea;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class LatexSyntaxHighlighter extends SyntaxHighlighterBase {

    /*
     * TextAttributesKeys
     */
    public static final TextAttributesKey BRACES = TextAttributesKey.createTextAttributesKey(
            "LATEX_BRACES",
            DefaultLanguageHighlighterColors.BRACES
    );

    public static final TextAttributesKey BRACKETS = TextAttributesKey.createTextAttributesKey(
            "LATEX_BRACKETS",
            DefaultLanguageHighlighterColors.BRACKETS
    );

    public static final TextAttributesKey COMMAND = TextAttributesKey.createTextAttributesKey(
            "LATEX_COMMAND",
            DefaultLanguageHighlighterColors.KEYWORD
    );

    public static final TextAttributesKey COMMENT = TextAttributesKey.createTextAttributesKey(
            "LATEX_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
    );

    public static final TextAttributesKey INLINE_MATH = TextAttributesKey.createTextAttributesKey(
            "LATEX_INLINE_MATH",
            DefaultLanguageHighlighterColors.STRING
    );

    public static final TextAttributesKey DISPLAY_MATH = TextAttributesKey.createTextAttributesKey(
            "LATEX_DISPLAY_MATH",
            DefaultLanguageHighlighterColors.STRING
    );

    /*
     * TextAttributeKey[]s
     */
    private static final TextAttributesKey[] BRACES_KEYS = new TextAttributesKey[] {
            BRACES
    };

    private static final TextAttributesKey[] BRACKET_KEYS = new TextAttributesKey[] {
            BRACKETS
    };

    private static final TextAttributesKey[] COMMAND_KEYS = new TextAttributesKey[] {
            COMMAND
    };

    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[] {
            COMMENT
    };

    private static final TextAttributesKey[] INLINE_MATH_KEYS = new TextAttributesKey[] {
            INLINE_MATH
    };

    private static final TextAttributesKey[] DISPLAY_MATH_KEYS = new TextAttributesKey[] {
            DISPLAY_MATH
    };

    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

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
        else if (tokenType.equals(LatexTypes.COMMAND_TOKEN)) {
            return COMMAND_KEYS;
        }
        // Math environment
        else if (tokenType.equals(LatexTypes.INLINE_MATH_DELIM)) {
            return INLINE_MATH_KEYS;
        }
        else if (tokenType.equals(LatexTypes.DISPLAY_MATH_START) ||
                tokenType.equals(LatexTypes.DISPLAY_MATH_END)) {
            return DISPLAY_MATH_KEYS;
        }
        // When no supported highlights is available
        else {
            return EMPTY_KEYS;
        }
    }
}
