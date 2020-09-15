package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.action.EditorAction
import nl.hannahsten.texifyidea.file.LatexFile

class MoveElementLeftAction : EditorAction("Move Element Left", null) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        println("move left action performed")
    }

    /**
     * The move element left action should only be enabled when we are in a LaTeX file.
     * Note that this function only updates the presentation of the action.
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.dataContext.getData("psi.File") is LatexFile
    }
}