package nl.rubensten.texifyidea.highlighting;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import nl.rubensten.texifyidea.file.LatexFile;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class LatexTypedHandler extends TypedHandlerDelegate {

    @Override
    public Result charTyped(char c, Project project, @NotNull Editor editor, @NotNull PsiFile
            file) {
        Result result = Result.CONTINUE;

        if (file instanceof LatexFile) {
            if (c == '$') {
                IElementType tokenType = getTypedTokenType(editor);

                if (tokenType != LatexTypes.COMMAND_TOKEN) {
                    editor.getDocument().insertString(
                            editor.getCaretModel().getOffset(),
                            String.valueOf(c)
                    );
                    return Result.STOP;
                }
            }
            else if (c == '[') {
                return insertDisplayMathClose(c, editor);
            }

        }

        return Result.CONTINUE;
    }

    /**
     * Upon typing {@code \[}, inserts the closing delimiter {@code \[}.
     */
    private Result insertDisplayMathClose(char c, Editor editor) {
        IElementType tokenType = getTypedTokenType(editor);

        if (tokenType == LatexTypes.DISPLAY_MATH_START) {
            editor.getDocument().insertString(editor.getCaretModel().getOffset(), "\\]");
            return Result.STOP;
        }

        return Result.CONTINUE;
    }

    /**
     * Retrieves the token type of the character just typed.
     */
    private IElementType getTypedTokenType(Editor editor) {
        int caret = editor.getCaretModel().getOffset();

        final EditorHighlighter highlighter = ((EditorEx)editor).getHighlighter();
        HighlighterIterator iterator = highlighter.createIterator(caret - 1);

        return iterator.getTokenType();
    }
}
