package nl.hannahsten.texifyidea.action;

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
import com.intellij.util.Consumer;
import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.file.*;
import nl.hannahsten.texifyidea.templates.LatexTemplatesFactory;
import nl.hannahsten.texifyidea.util.files.FileUtil;
import nl.hannahsten.texifyidea.util.Magic;
import nl.hannahsten.texifyidea.util.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Hannah Schellekens
 */
public class NewLatexFileAction extends CreateElementActionBase {

    private static final String OPTION_TEX_FILE = "tex";
    private static final String OPTION_STY_FILE = "sty";
    private static final String OPTION_CLS_FILE = "cls";
    private static final String OPTION_BIB_FILE = "bib";
    private static final String OPTION_TIKZ_FILE = "tikz";

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
        builder.addKind("Bibliography (.bib)", TexifyIcons.BIBLIOGRAPHY_FILE, OPTION_BIB_FILE);
        builder.addKind("Package (.sty)", TexifyIcons.STYLE_FILE, OPTION_STY_FILE);
        builder.addKind("Document class (.cls)", TexifyIcons.CLASS_FILE, OPTION_CLS_FILE);
        builder.addKind("TikZ (.tikz)", TexifyIcons.TIKZ_FILE, OPTION_TIKZ_FILE);
        Consumer<PsiElement> consumer = (el) -> {};
        builder.show("", null, fileCreator, consumer);

        return fileCreator.getCreatedElements();
    }

    @NotNull
    @Override
    protected PsiElement[] create(String s, PsiDirectory psiDirectory) throws Exception {
        return Magic.General.emptyPsiElementArray;
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
            return Magic.General.emptyPsiElementArray;
        }

        private String getTemplateNameFromExtension(String extensionWithoutDot) {
            switch (extensionWithoutDot) {
                case OPTION_STY_FILE:
                    return LatexTemplatesFactory.fileTemplateSty;
                case OPTION_CLS_FILE:
                    return LatexTemplatesFactory.fileTemplateCls;
                case OPTION_BIB_FILE:
                    return LatexTemplatesFactory.fileTemplateBib;
                case OPTION_TIKZ_FILE:
                    return LatexTemplatesFactory.fileTemplateTikz;
                default:
                    return LatexTemplatesFactory.fileTemplateTex;
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

            if (smallFileName.endsWith("." + OPTION_BIB_FILE)) {
                return BibtexFileType.INSTANCE;
            }

            if (smallFileName.endsWith("." + OPTION_TIKZ_FILE)) {
                return TikzFileType.INSTANCE;
            }

            return FileUtil.fileTypeByExtension(option);
        }

        private String getNewFileName(@NotNull String fileName, FileType fileType) {
            String smallFileName = fileName.toLowerCase();

            if (smallFileName.endsWith("." + fileType.getDefaultExtension())) {
                return smallFileName;
            }

            return StringsKt.appendExtension(fileName, fileType.getDefaultExtension());
        }

        @Nullable
        @Override
        public PsiElement createFile(@NotNull String fileName, @NotNull String option) {
            FileType fileType = getFileType(fileName, option);
            String newFileName = getNewFileName(fileName, fileType);
            String templateName = getTemplateNameFromExtension(fileType.getDefaultExtension());

            PsiFile file = LatexTemplatesFactory.Companion.createFromTemplate(directory, newFileName,
                    templateName, fileType);
            openFile(file.getVirtualFile());
            return file;
        }

        @NotNull
        @Override
        public String getActionName(@NotNull String fileName, @NotNull String option) {
            return "New LaTeX File";
        }

        @Override
        public boolean startInWriteAction() {
            return false;
        }
    }
}
