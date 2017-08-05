package nl.rubensten.texifyidea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import nl.rubensten.texifyidea.file.LatexFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;

/**
 * Action that fetches the required information beforehand.
 *
 * @author Ruben Schellekens
 */
public abstract class EditorAction extends AnAction {

    /**
     * The name of the action.
     */
    private final String name;

    public EditorAction(@Nullable String name, @Nullable Icon icon) {
        super(name, null, icon);
        this.name = name;
    }

    /**
     * Gets called every time the action gets executed.
     *
     * @param file
     *         The file that was focussed on when the action was performed.
     * @param project
     *         The project that was open when the action was performed.
     * @param editor
     *         The active editor when the action was performed.
     */
    public abstract void actionPerformed(VirtualFile file, Project project, TextEditor editor);

    /**
     * Get the PsiElement that is selected in the editor.
     */
    @Nullable
    protected PsiElement getElement(VirtualFile file, Project project, TextEditor editor) {
        if (file == null || project == null || editor == null) {
            return null;
        }

        PsiFile psiFile = getPsiFile(file, project);
        if (file == null || !(psiFile instanceof LatexFile)) {
            return null;
        }

        int offset = editor.getEditor().getCaretModel().getOffset();
        return psiFile.findElementAt(offset);
    }

    @Nullable
    protected PsiFile getPsiFile(VirtualFile file, Project project) {
        if (file == null || project == null) {
            return null;
        }

        return PsiManager.getInstance(project).findFile(file);
    }

    protected void runWriteAction(@NotNull Project project, @NotNull Runnable writeAction) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            CommandProcessor.getInstance().executeCommand(project, writeAction, getName(), "Texify");
        });
    }

    @Override
    final public void actionPerformed(AnActionEvent event) {
        VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        Project project = event.getData(PlatformDataKeys.PROJECT);
        if (file == null || project == null) {
            return;
        }

        TextEditor editor = getTextEditor(project, event.getData(PlatformDataKeys.FILE_EDITOR));
        if (editor == null) {
            return;
        }

        actionPerformed(file, project, editor);
    }

    @Nullable
    private TextEditor getTextEditor(Project project, FileEditor fileEditor) {
        if (fileEditor instanceof TextEditor) {
            return (TextEditor)fileEditor;
        }

        if (project == null) {
            return null;
        }

        FileEditor[] editors = FileEditorManager.getInstance(project).getSelectedEditors();
        return Arrays.stream(editors)
                .filter(e -> e instanceof TextEditor)
                .map(e -> (TextEditor)e)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the name of the action.
     */
    public String getName() {
        return name;
    }
}
