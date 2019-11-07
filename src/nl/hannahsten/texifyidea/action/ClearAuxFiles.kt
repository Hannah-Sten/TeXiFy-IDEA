package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.util.Magic
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Action to delete all auxiliary files.
 *
 * @author Abby Berkers
 */
class ClearAuxFiles : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return
        val basePath = project.basePath ?: return
        val files = FileUtils.listFiles(File(basePath), Magic.File.auxiliaryFileTypes, true)
        files.forEach { it.delete() }
        LocalFileSystem.getInstance().refresh(true)
    }
}