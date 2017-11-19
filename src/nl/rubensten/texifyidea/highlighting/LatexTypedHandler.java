package nl.rubensten.texifyidea.highlighting;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.file.LatexFile;
import nl.rubensten.texifyidea.psi.LatexInlineMath;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class LatexTypedHandler extends TypedHandlerDelegate {

    @Override
    public Result beforeCharTyped(char c, Project project, Editor editor, PsiFile file, FileType fileType) {
        if (file instanceof LatexFile) {
            if (c == '$') {
                CaretModel caret = editor.getCaretModel();
                PsiElement element = file.findElementAt(caret.getOffset());
                LatexInlineMath parent = PsiTreeUtil.getParentOfType(element, LatexInlineMath.class);

                if (parent == null) {
                    return Result.CONTINUE;
                }

                int endOffset = parent.getTextRange().getEndOffset();

                if (caret.getOffset() == endOffset - 1) {
                    // Caret is at the end of the environment, so run over the closing $
                    caret.moveCaretRelatively(1, 0, false, false, true);
                    return Result.STOP;
                }
            }
        }

        return Result.CONTINUE;
    }

    @Override
    public Result charTyped(char c, Project project, @NotNull Editor editor, @NotNull PsiFile
            file) {

        if (file instanceof LatexFile) {
            if (c == '$') {
                IElementType tokenType = getTypedTokenType(editor);

                if (tokenType != LatexTypes.COMMAND_TOKEN && tokenType != LatexTypes.COMMENT_TOKEN) {
                    editor.getDocument().insertString(
                            editor.getCaretModel().getOffset(),
                            String.valueOf(c)
                    );
                    return Result.STOP;
                }
            }
            else if (c == '[') {
                return insertDisplayMathClose(editor);
            }
            else if (c == '(') {
                return insertRobustInlineMathClose(editor);
            }

        }

        return Result.CONTINUE;
    }

    /**
     * Upon typing {@code \[}, inserts the closing delimiter {@code \]}.
     */
    private Result insertDisplayMathClose(Editor editor) {
        IElementType tokenType = getTypedTokenType(editor);

        if (tokenType == LatexTypes.DISPLAY_MATH_START) {
            editor.getDocument().insertString(editor.getCaretModel().getOffset(), "\\]");
            return Result.STOP;
        }

        return Result.CONTINUE;
    }

    /**
     * Upon typing {@code \(}, inserts the closing delimiter {@code \)} with spaces in between.
     */
    private Result insertRobustInlineMathClose(Editor editor) {
        IElementType tokenType = getTypedTokenType(editor);

        if (tokenType == LatexTypes.INLINE_MATH_START) {
            // Only insert backslash because the closing parenthesis is already inserted by the PairedBraceMatcher.
            editor.getDocument().insertString(editor.getCaretModel().getOffset(), "  \\");
            editor.getCaretModel().moveCaretRelatively(1, 0, false, false, false);

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
