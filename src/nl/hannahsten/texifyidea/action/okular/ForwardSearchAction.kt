package nl.hannahsten.texifyidea.action.okular

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.EditorAction
import nl.hannahsten.texifyidea.run.okular.isOkularAvailable

/**
 * @author Abby Berkers
 */
open class ForwardSearchAction : EditorAction(
        "_ForwardSearch",
        TexifyIcons.RIGHT
) {
    override fun actionPerformed(file: VirtualFile?, project: Project?, editor: TextEditor?) {
        // Todo forward search.
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = isOkularAvailable()
    }
}