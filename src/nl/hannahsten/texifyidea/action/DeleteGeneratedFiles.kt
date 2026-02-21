package nl.hannahsten.texifyidea.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.showOkCancelDialog
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCleanUtil
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.selectedRunConfig
import nl.hannahsten.texifyidea.util.magic.FileMagic
import nl.hannahsten.texifyidea.util.runWriteAction
import java.io.File
import java.io.IOException
import java.security.PrivilegedActionException

/**
 * Similar to [DeleteAuxFiles].
 */
class DeleteGeneratedFiles : AnAction() {

    // This action is disabled by default because it deletes directories so is unsafe, see #3983
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = Registry.`is`("texify.delete.generated.files")
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

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

        val selectedRunConfig = project.selectedRunConfig()
        if (selectedRunConfig is LatexRunConfiguration && selectedRunConfig.compiler == LatexCompiler.LATEXMK) {
            LatexmkCleanUtil.run(project, selectedRunConfig, LatexmkCleanUtil.Mode.CLEAN_ALL)
            return
        }

        // Custom output folders
        val customOutput = project.getLatexRunConfigurations()
            .flatMap { config ->
                val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(config)
                listOf(
                    LatexPathResolver.resolveOutputDir(config, mainFile),
                    LatexPathResolver.resolveAuxDir(config, mainFile)
                )
            }
            // There's no reason to delete files outside the project
            .filter { it?.path?.contains(project.basePath!!) == true }
            .filterNotNull()

        val result = showOkCancelDialog(
            "Delete Auxiliary and Output Files",
            "Do you really want to delete all files in LaTeX output directories, " +
                "and all auxiliary and generated files? \n" +
                "All files in the following output directories will be deleted: \n" +
                customOutput.map { it.path }.joinToString { "  $it\n" } +
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

        val notDeleted = mutableListOf<String>()

        // Custom out/aux dirs
        runWriteAction {
            for (path in customOutput) {
                try {
                    path.children?.forEach {
                        try {
                            it.delete(this)
                        }
                        catch (e: IOException) {
                            Log.warn(e.message ?: e.toString())
                            notDeleted.add(it.path)
                        }
                    }
                }
                catch (e: InvalidVirtualFileAccessException) {
                    Log.warn(e.message ?: e.toString())
                    notDeleted.add(path.path)
                }
            }
        }

        if (notDeleted.isNotEmpty()) {
            Notification("LaTeX", "Could not delete some files", "The following files need to be deleted manually: $notDeleted", NotificationType.WARNING).notify(project)
        }

        // Generated minted files
        File(basePath, "src").walk().maxDepth(1)
            .filter { it.name.startsWith("_minted") }
            .forEach { it.deleteRecursively() }

        LocalFileSystem.getInstance().refresh(true)
    }
}
