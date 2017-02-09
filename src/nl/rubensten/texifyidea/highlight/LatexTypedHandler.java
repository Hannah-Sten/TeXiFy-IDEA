package nl.rubensten.texifyidea.highlight;

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
                int caret = editor.getCaretModel().getOffset();

                final EditorHighlighter highlighter = ((EditorEx)editor).getHighlighter();
                HighlighterIterator iterator = highlighter.createIterator(caret - 1);
                IElementType tokenType = iterator.getTokenType();

                if (tokenType != LatexTypes.COMMAND_TOKEN) {
                    editor.getDocument().insertString(caret, String.valueOf(c));
                    result = Result.STOP;
                }
            }
        }

        return result;
    }
}
