package nl.rubensten.texifyidea.action.insert;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.action.InsertEditorAction;
import nl.rubensten.texifyidea.util.PackageUtils;

/**
 * @author Ruben Schellekens
 */
public class InsertStrikethroughAction extends InsertEditorAction {

    public InsertStrikethroughAction() {
        super("Strikethrough (ulem package)", TexifyIcons.FONT_STRIKETHROUGH, "\\sout{", "}");
    }

    @Override
    public void actionPerformed(VirtualFile file, Project project, TextEditor textEditor) {
        super.actionPerformed(file, project, textEditor);

        final Document document = textEditor.getEditor().getDocument();
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        final String packageName = "ulem";

        runWriteAction(project, () ->
                PackageUtils.insertUsepackage(document, psiFile, packageName, null)
        );
    }
}
