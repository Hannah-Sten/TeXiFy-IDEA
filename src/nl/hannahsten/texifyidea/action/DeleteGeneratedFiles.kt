package nl.hannahsten.texifyidea.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.showOkCancelDialog
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.magic.FileMagic
import nl.hannahsten.texifyidea.util.runWriteAction
import java.io.File
import java.io.IOException
import java.security.PrivilegedActionException

/**
 * Similar to [DeleteAuxFiles].
 */
class DeleteGeneratedFiles : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        try {
            deleteFiles(event)
        }
        catch (e: PrivilegedActionException) {
            Notification("LaTeX", "Could not delete some files", e.message ?: "", NotificationType.ERROR).notify(event.project)
        }
    }

    private fun deleteFiles(e: AnActionEvent) {
        val project = getEventProject(e) ?: return
        val basePath = project.basePath ?: return

        // Custom output folders
        val customOutput = project.getLatexRunConfigurations()
            .flatMap { listOf(it.outputPath.getAndCreatePath(), it.auxilPath.getAndCreatePath()) }
            // There's no reason to delete files outside the project
            .filter { it?.path?.contains(project.basePath!!) == true }

        val result = showOkCancelDialog(
            "Delete Auxiliary and Output Files",
        "Do you really want to delete all files in LaTeX output directories, " +
                "and all auxiliary and generated files? \n" +
                "All files in the following output directories will be deleted: \n" +
                customOutput.mapNotNull { it?.path }.joinToString { "  $it\n" } +
                "plus auxiliary and generated files in src/, auxil/ and out/.\n" +
                "Be careful when doing this, you might not be able to fully undo this operation!",
            "Delete"
        )

        if (result != Messages.OK) return

        // Also clear aux files
        DeleteAuxFiles().actionPerformed(e)

        // Delete files only in specific folders, to avoid deleting for example figures with pdf extension
        for (folder in setOf("src")) {
            File(basePath, folder).walk().maxDepth(1)
                .filter { it.isFile }
                .filter { it.extension in FileMagic.generatedFileTypes }
                .forEach { it.delete() }
        }

        // Just delete everything in directories which should only contain output files
        val defaultOutput = setOf(File(basePath, "auxil"), File(basePath, "out"))
        for (path in defaultOutput) {
            path.walk().maxDepth(1).forEach { it.delete() }
        }

        // Custom out/aux dirs
        runWriteAction {
            for (path in customOutput) {
                path?.children?.forEach { it.delete(this) }
            }
        }

        // Generated minted files
        File(basePath, "src").walk().maxDepth(1)
            .filter { it.name.startsWith("_minted") }
            .forEach { it.deleteRecursively() }

        LocalFileSystem.getInstance().refresh(true)
    }
}