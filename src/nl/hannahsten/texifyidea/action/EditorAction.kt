package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.file.LatexFile
import java.util.*
import javax.swing.Icon

/**
 * Action that fetches the required information beforehand.
 *
 * @author Hannah Schellekens
 */
abstract class EditorAction(val name: String, icon: Icon?) : AnAction(name, null, icon) {

    /**
     * Gets called every time the action gets executed.
     *
     * @param file
     * The file that was focussed on when the action was performed.
     * @param project
     * The project that was open when the action was performed.
     * @param textEditor
     * The active editor when the action was performed.
     */
    abstract fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor)

    /**
     * Get the PsiElement that is selected in the editor.
     */
    protected fun getElement(file: VirtualFile, project: Project, textEditor: TextEditor): PsiElement? {
        val psiFile = getPsiFile(file, project) as? LatexFile ?: return null

        val offset = textEditor.editor.caretModel.offset
        return psiFile.findElementAt(offset)
    }

    protected fun getPsiFile(file: VirtualFile, project: Project): PsiFile? {
        return PsiManager.getInstance(project).findFile(file)
    }

    protected fun runWriteAction(project: Project, file: VirtualFile, writeAction: () -> Unit) {
        if (file.isWritable) {
            ApplicationManager.getApplication().runWriteAction {
                CommandProcessor.getInstance().executeCommand(project, writeAction, name, "Texify")
            }
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(PlatformDataKeys.VIRTUAL_FILE)
        val project = event.getData(PlatformDataKeys.PROJECT)
        if (file == null || project == null) {
            return
        }

        val editor = getTextEditor(project, event.getData(PlatformDataKeys.FILE_EDITOR)) ?: return

        actionPerformed(file, project, editor)
    }

    fun getTextEditor(project: Project, fileEditor: FileEditor?): TextEditor? {
        if (fileEditor is TextEditor) {
            return fileEditor
        }

        val editors = FileEditorManager.getInstance(project).selectedEditors
        return Arrays.stream(editors)
            .filter { e -> e is TextEditor }
            .map { e -> e as TextEditor }
            .findFirst()
            .orElse(null)
    }
}
