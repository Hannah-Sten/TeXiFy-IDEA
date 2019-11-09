package nl.hannahsten.texifyidea.action.evince

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.EditorAction
import nl.hannahsten.texifyidea.run.evince.EvinceConversation
import nl.hannahsten.texifyidea.run.evince.isEvinceAvailable

/**
 * Starts a forward search action in Evince.
 *
 * Note: this is only available on Linux.
 *
 * @author Thomas Schouten
 */
open class ForwardSearchAction : EditorAction(
        "_ForwardSearch",
        TexifyIcons.RIGHT
) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (!isEvinceAvailable()) {
            return
        }

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1

        EvinceConversation.forwardSearch(sourceFilePath = file.path, line = line)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = isEvinceAvailable()
    }
}