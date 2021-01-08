package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.showOkCancelDialog
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.util.magic.FileMagic
import java.io.File

/**
 * Similar to [ClearAuxFiles].
 */
class ClearGeneratedFiles : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return
        val basePath = project.basePath ?: return

        // Delete generated files only in these folders
        for (folder in setOf("src", "auxil", "out")) {
            File(basePath, folder).walk().maxDepth(1)
                .filter { it.isFile }
                .filter { it.extension in FileMagic.generatedFileTypes }
                .forEach { it.delete() }
        }

        // Generated minted files
        File(basePath, "src").walk().maxDepth(1)
            .filter { it.name.startsWith("_minted") }
            .forEach { it.deleteRecursively() }

        showOkCancelDialog(
            "Clear auxiliary and generated files",
            "Delete all LaTeX auxiliary and generated files? \n" +
                "All auxiliary and generated files in src/, auxil/ and out/ will be deleted, \n" +
                "including for example pdf and log files. \n" +
                "You might not be able to fully undo this operation!",
            "Delete"
        )
        LocalFileSystem.getInstance().refresh(true)
    }
}