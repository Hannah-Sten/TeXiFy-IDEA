package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.showOkCancelDialog
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.magic.FileMagic
import nl.hannahsten.texifyidea.util.runWriteAction
import java.io.File

/**
 * Similar to [ClearAuxFiles].
 */
class ClearGeneratedFiles : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val result = showOkCancelDialog(
            "Clear Auxiliary and Generated Files",
            "Delete all LaTeX auxiliary and generated files? \n" +
                "All auxiliary and generated files in output directories will be deleted, \n" +
                "including for example pdf and log files. \n" +
                "You might not be able to fully undo this operation!",
            "Delete"
        )

        if (result != Messages.OK) return

        val project = getEventProject(e) ?: return
        val basePath = project.basePath ?: return

        // Also clear aux files
        ClearAuxFiles().actionPerformed(e)

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
        val customOutput = project.getLatexRunConfigurations().flatMap { listOf(it.outputPath.getAndCreatePath(), it.auxilPath.getAndCreatePath()) }
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