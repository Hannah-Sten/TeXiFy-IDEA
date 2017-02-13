package nl.rubensten.texifyidea.action;

import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.file.LatexFileType;
import nl.rubensten.texifyidea.templates.LatexTemplatesFactory;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * @author Ruben Schellekens
 */
public class NewLatexFileAction extends CreateElementActionBase implements DumbAware {

    private static final String OPTION_TEX_FILE = "tex";
    private static final String OPTION_STY_FILE = "sty";
    private static final String OPTION_CLS_FILE = "cls";

    public NewLatexFileAction() {
        super("LaTeX File", "Create a new LaTeX file", TexifyIcons.LATEX_FILE);
    }

    @NotNull
    @Override
    protected PsiElement[] invokeDialog(Project project, PsiDirectory psiDirectory) {
        LatexFileCreator fileCreator = new LatexFileCreator(project, psiDirectory);

        CreateFileFromTemplateDialog.Builder builder = CreateFileFromTemplateDialog.createDialog(project);
        builder.setTitle("Create a New LaTeX File");
        builder.addKind("Sources (.tex)", TexifyIcons.LATEX_FILE, OPTION_TEX_FILE);
        builder.addKind("Package (.sty)", TexifyIcons.STYLE_FILE, OPTION_STY_FILE);
        builder.addKind("Document class (.cls)", TexifyIcons.CLASS_FILE, OPTION_CLS_FILE);
        builder.show("", null, fileCreator);

        return new PsiElement[] {
                fileCreator.getCreatedFile()
        };
    }

    @NotNull
    @Override
    protected PsiElement[] create(String newName, PsiDirectory psiDirectory) throws Exception {
        PsiFile file = LatexTemplatesFactory.createFromTemplate(
                psiDirectory,
                TexifyUtil.appendExtension(newName, "tex"),
                LatexTemplatesFactory.DEFAULT_TEMPLATE_FILENAME,
                LatexFileType.INSTANCE);

        PsiElement child = file.getLastChild();
        if (child == null) {
            return new PsiElement[] {
                    file
            };
        }
        else {
            return new PsiElement[] {
                    file,
                    child
            };
        }
    }

    @Override
    protected String getErrorTitle() {
        return "Error";
    }

    @Override
    protected String getCommandName() {
        return "LaTeX File";
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s) {
        return "LaTeX File";
    }

    private class LatexFileCreator implements CreateFileFromTemplateDialog.FileCreator<PsiElement> {

        private final Project project;
        private final VirtualFile directory;
        private final LocalFileSystem fileSystem;

        private PsiFile createdFile;

        private LatexFileCreator(Project project, PsiDirectory directory) {
            this.project = project;
            this.directory = directory.getVirtualFile();
            this.fileSystem = LocalFileSystem.getInstance();
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
            return (createdFile = PsiManager.getInstance(project).findFile(virtualFile));
        }

        @NotNull
        @Override
        public String getActionName(@NotNull String fileName, @NotNull String option) {
            return "New LaTeX File";
        }

        public PsiFile getCreatedFile() {
            return createdFile;
        }
    }
}
