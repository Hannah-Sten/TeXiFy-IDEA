package nl.rubensten.texifyidea.action;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import nl.rubensten.texifyidea.TexifyIcons;

/**
 * @author Ruben Schellekens
 */
public class NewLatexFileAction extends AnAction {

    public NewLatexFileAction() {
        super("LaTeX File", "Create a new LaTeX file", TexifyIcons.LATEX_FILE);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);

        CreateFileFromTemplateDialog.Builder builder = CreateFileFromTemplateDialog.createDialog(project);
        builder.addKind("Test TeX", TexifyIcons.LATEX_FILE, "Thingy TeX");
        builder.addKind("Test Sty", TexifyIcons.STYLE_FILE, "Thingy Sty");
        builder.addKind("Test Cls", TexifyIcons.CLASS_FILE, "Thingy cls");
    }
}
