package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.run.latex.FileCleanupSupport
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCleanUtil
import nl.hannahsten.texifyidea.util.Log
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
            LatexmkCleanUtil.run(project, selectedRunConfig, LatexmkCleanUtil.Mode.CLEAN)
            return
        }

        val basePath = project.basePath ?: return
        val result = FileCleanupSupport.delete(
            FileCleanupSupport.collectProjectTemporaryBuildTargets(Path.of(basePath))
        )
        if (result.failedPaths.isNotEmpty()) {
            Log.warn("Could not delete some temporary build files: ${result.failedPaths.joinToString()}")
        }
        LocalFileSystem.getInstance().refresh(true)
    }
}
