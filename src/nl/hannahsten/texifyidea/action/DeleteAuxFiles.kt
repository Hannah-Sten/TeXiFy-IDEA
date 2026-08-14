package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.showOkCancelDialog
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.latex.FileCleanupSupport
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCleanUtil
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.selectedRunConfig
import java.nio.file.Path

/**
 * Action to delete all auxiliary files.
 *
 * @author Abby Berkers
 */
class DeleteAuxFiles : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return

        val selectedRunConfig = project.selectedRunConfig()
        if (selectedRunConfig is LatexRunConfiguration && selectedRunConfig.hasEnabledLatexmkStep()) {
            val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(selectedRunConfig) ?: return
            val command = LatexmkCleanUtil.buildCleanCommandForModel(selectedRunConfig, mainFile, false) ?: return
            val workingDirectoryPath = LatexPathResolver.resolve(selectedRunConfig.workingDirectory, mainFile, project) ?: Path.of(mainFile.parent.path)

            val result = showOkCancelDialog(
                TexifyBundle.message("action.delete.auxiliary.files.title"),
                TexifyBundle.message("action.delete.auxiliary.files.confirmation", command.joinToString(" "), workingDirectoryPath),
                TexifyBundle.message("ui.dialog.delete.generated.files.ok")
            )
            if (result == Messages.OK) {
                LatexmkCleanUtil.run(project, selectedRunConfig, LatexmkCleanUtil.Mode.CLEAN)
                return
            }
        }

        val configurations = project.getLatexRunConfigurations()
        val paths = configurations.mapNotNull { runConfig ->
            val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig)
            LatexPathResolver.resolve(runConfig.outputPath, mainFile, project)
        }.distinct()
        val result = showOkCancelDialog(
            TexifyBundle.message("action.delete.auxiliary.files.title"),
            TexifyBundle.message("action.delete.auxiliary.files.directory.confirmation", paths.joinToString(", ")),
            TexifyBundle.message("ui.dialog.delete.generated.files.ok")
        )
        if (result != Messages.OK) return
        paths.forEach {
            val result = FileCleanupSupport.delete(
                FileCleanupSupport.collectProjectTemporaryBuildTargets(it)
            )
            if (result.failedPaths.isNotEmpty()) {
                Log.warn("Could not delete some temporary build files: ${result.failedPaths.joinToString()}")
            }
        }
        LocalFileSystem.getInstance().refresh(true)
    }
}
