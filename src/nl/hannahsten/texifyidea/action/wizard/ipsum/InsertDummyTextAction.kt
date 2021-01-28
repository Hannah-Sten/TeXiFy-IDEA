package nl.hannahsten.texifyidea.action.wizard.ipsum

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.util.caretOffset
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.lineIndentationByOffset

/**
 * @author Hannah Schellekens
 */
open class InsertDummyTextAction : AnAction() {

    /**
     * Opens and handles the dummy text UI.
     */
    fun executeAction(project: Project) {
        val editor = project.currentTextEditor() ?: return
        val document = editor.editor.document

        // Get the indentation from the current line.
        val indent = document.lineIndentationByOffset(editor.editor.caretOffset())

        // Create the dialog.
        val dialog = InsertDummyTextDialogWrapper()

        // If the user pressed OK, do stuff.
        if (dialog.showAndGet().not()) return

        // TODO: insert actual stuff.
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT) ?: return
        executeAction(project)
    }
}