package nl.rubensten.texifyidea.action;

import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.file.ClassFileType;
import nl.rubensten.texifyidea.file.LatexFileType;
import nl.rubensten.texifyidea.file.StyleFileType;
import nl.rubensten.texifyidea.templates.LatexTemplatesFactory;
import nl.rubensten.texifyidea.util.Constants;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ruben Schellekens
 */
public class NewLatexFileAction extends CreateElementActionBase {

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

        return fileCreator.getCreatedElements();
    }

    @NotNull
    @Override
    protected PsiElement[] create(String s, PsiDirectory psiDirectory) throws Exception {
        return Constants.EMPTY_PSI_ELEMENT_ARRAY;
    }

    @Override
    protected String getErrorTitle() {
        return "Error";
    }

    @Override
    protected String getCommandName() {
        return null;
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s) {
        return null;
    }

    private class LatexFileCreator implements CreateFileFromTemplateDialog.FileCreator<PsiElement> {

        private final Project project;
        private final PsiDirectory directory;
        private final LocalFileSystem fileSystem;

        private LatexFileCreator(Project project, PsiDirectory directory) {
            this.project = project;
            this.directory = directory;
            this.fileSystem = LocalFileSystem.getInstance();
        }

        private void openFile(VirtualFile virtualFile) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            fileEditorManager.openFile(virtualFile, true);
        }

        public PsiElement[] getCreatedElements() {
            return Constants.EMPTY_PSI_ELEMENT_ARRAY;
        }

        private String getTemplateNameFromExtension(String extensionWithoutDot) {
            switch (extensionWithoutDot) {
                case OPTION_STY_FILE:
                    return LatexTemplatesFactory.FILE_TEMPLATE_STY;
                case OPTION_CLS_FILE:
                    return LatexTemplatesFactory.FILE_TEMPLATE_CLS;
                default:
                    return LatexTemplatesFactory.FILE_TEMPLATE_TEX;
            }
        }

        private FileType getFileType(@NotNull String fileName, String option) {
            String smallFileName = fileName.toLowerCase();

            if (smallFileName.endsWith("." + OPTION_TEX_FILE)) {
                return LatexFileType.INSTANCE;
            }

            if (smallFileName.endsWith("." + OPTION_CLS_FILE)) {
                return ClassFileType.INSTANCE;
            }

            if (smallFileName.endsWith("." + OPTION_STY_FILE)) {
                return StyleFileType.INSTANCE;
            }

            return TexifyUtil.getFileTypeByExtension(option);
        }

        private String getNewFileName(@NotNull String fileName, FileType fileType) {
            String smallFileName = fileName.toLowerCase();

            if (smallFileName.endsWith("." + fileType.getDefaultExtension())) {
                return smallFileName;
            }

            return TexifyUtil.appendExtension(smallFileName, fileType.getDefaultExtension());
        }

        @Nullable
        @Override
        public PsiElement createFile(@NotNull String fileName, @NotNull String option) {
            FileType fileType = getFileType(fileName, option);
            String newFileName = getNewFileName(fileName, fileType);
            String templateName = getTemplateNameFromExtension(fileType.getDefaultExtension());

            PsiFile file = LatexTemplatesFactory.createFromTemplate(directory, newFileName,
                    templateName, fileType);
            openFile(file.getVirtualFile());
            return file;
        }

        @NotNull
        @Override
        public String getActionName(@NotNull String fileName, @NotNull String option) {
            return "New LaTeX File";
        }
    }
}
