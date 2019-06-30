package nl.hannahsten.texifyidea.action;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Hannah Schellekens
 */
public class InsertEditorAction extends EditorAction {

    /**
     * What to insert before the selection.
     */
    private String before;

    /**
     * What to insert after the selection.
     */
    private String after;

    /**
     * @param name
     *         The name of the action.
     * @param icon
     *         The icon of the action.
     * @param before
     *         The text to insert before the selection.
     * @param after
     *         The text to insert after the selection.
     */
    public InsertEditorAction(@Nullable String name, @Nullable Icon icon,
                              @Nullable String before, @Nullable String after) {
        super(name, icon);

        this.before = (before == null ? "" : before);
        this.after = (after == null ? "" : after);
    }

    @Override
    public void actionPerformed(VirtualFile file, Project project, TextEditor textEditor) {
        final Editor editor = textEditor.getEditor();
        final Document document = editor.getDocument();
        final SelectionModel selection = editor.getSelectionModel();
        final int start = selection.getSelectionStart();
        final int end = selection.getSelectionEnd();

        runWriteAction(project, () -> insert(document, start, end, editor.getCaretModel()));
    }

    private void insert(Document document, int start, int end, CaretModel caretModel) {
        document.insertString(end, this.after);
        document.insertString(start, this.before);

        int caretPosition;
        if (start == end) {
            caretPosition = start + this.before.length();
        }
        else {
            caretPosition = end + this.before.length() + this.after.length();
        }

        caretModel.moveToOffset(caretPosition);
    }
}
