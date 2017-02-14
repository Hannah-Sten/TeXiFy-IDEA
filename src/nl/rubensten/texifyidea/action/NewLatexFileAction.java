package nl.rubensten.texifyidea.action;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * @author Ruben Schellekens
 */
public class NewLatexFileAction extends AnAction implements DumbAware {

    private static final String OPTION_TEX_FILE = "tex";
    private static final String OPTION_STY_FILE = "sty";
    private static final String OPTION_CLS_FILE = "cls";

    public NewLatexFileAction() {
        super("LaTeX File", "Create a new LaTeX file", TexifyIcons.LATEX_FILE);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        final VirtualFile directory = event.getData(PlatformDataKeys.VIRTUAL_FILE);

        CreateFileFromTemplateDialog.Builder builder = CreateFileFromTemplateDialog.createDialog(project);
        builder.setTitle("Create a New LaTeX File");
        builder.addKind("Sources (.tex)", TexifyIcons.LATEX_FILE, OPTION_TEX_FILE);
        builder.addKind("Package (.sty)", TexifyIcons.STYLE_FILE, OPTION_STY_FILE);
        builder.addKind("Document class (.cls)", TexifyIcons.CLASS_FILE, OPTION_CLS_FILE);
        builder.show("", null, new LatexFileCreator(project, directory));
    }

    private class LatexFileCreator implements CreateFileFromTemplateDialog.FileCreator<PsiElement> {

        private final Project project;
        private final VirtualFile directory;
        private final LocalFileSystem fileSystem;

        private LatexFileCreator(Project project, VirtualFile directory) {
            this.project = project;
            this.directory = directory;
            this.fileSystem = LocalFileSystem.getInstance();
        }

        private void openFile(VirtualFile virtualFile) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            fileEditorManager.openFile(virtualFile, true);
        }

        @Nullable
        @Override
        public PsiElement createFile(@NotNull String fileName, @NotNull String option) {
            if (directory == null) {
                return null;
            }

            String dirPath = directory.getPath();
            String newFilePath = TexifyUtil.appendExtension(dirPath + "/" + fileName, option);
            File newFile = new File(newFilePath);

            try {
                newFile.createNewFile();
            }
            catch (IOException e) {
                throw new RuntimeException("Couldn't create new file!", e);
            }

            VirtualFile virtualFile = fileSystem.refreshAndFindFileByPath(newFilePath);
            openFile(virtualFile);

            return PsiManager.getInstance(project).findFile(virtualFile);
        }

        @NotNull
        @Override
        public String getActionName(@NotNull String fileName, @NotNull String option) {
            return "New LaTeX File";
        }
    }
}
