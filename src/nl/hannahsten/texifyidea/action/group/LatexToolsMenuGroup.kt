package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * @author Hannah Schellekens
 */
open class LatexToolsMenuGroup : DefaultActionGroup() {

    override fun update(event: AnActionEvent) {
        // Show menu when any LaTeX files are in the project
        val basePath = AnAction.getEventProject(event)?.basePath ?: return
        val files = FileUtils.listFiles(File(basePath), arrayOf("tex"), true)
        event.presentation.isEnabledAndVisible = !files.isEmpty()
    }
}