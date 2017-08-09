package nl.rubensten.texifyidea.action.sumatra;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import nl.rubensten.texifyidea.action.EditorAction;
import nl.rubensten.texifyidea.run.SumatraConversation;

/**
 * @author Sten Wessel
 */
public class ForwardSearchAction extends EditorAction {

    public ForwardSearchAction() {
        super("Forward search", null);
    }

    @Override
    public void actionPerformed(VirtualFile file, Project project, TextEditor editor) {
        if (!SystemInfo.isWindows) {
            return;
        }

        Document document = editor.getEditor().getDocument();
        int line = document.getLineNumber(editor.getEditor().getCaretModel().getOffset()) + 1;

        SumatraConversation.INSTANCE.forwardSearch(null, file.getPath(), line, false, false);
    }
}
