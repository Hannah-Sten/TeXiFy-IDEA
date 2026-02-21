package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCleanUtil
import nl.hannahsten.texifyidea.util.selectedRunConfig
import nl.hannahsten.texifyidea.util.magic.FileMagic
import java.io.File

/**
 * Action to delete all auxiliary files.
 *
 * @author Abby Berkers
 */
class DeleteAuxFiles : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return

        val selectedRunConfig = project.selectedRunConfig()
        if (selectedRunConfig is LatexRunConfiguration && selectedRunConfig.compiler == LatexCompiler.LATEXMK) {
            LatexmkCleanUtil.run(project, selectedRunConfig, LatexmkCleanUtil.Mode.CLEAN)
            return
        }

        val basePath = project.basePath ?: return
        File(basePath).walk().filter { it.isFile }
            .filter { it.extension in FileMagic.auxiliaryFileTypes }
            .forEach { it.delete() }
        LocalFileSystem.getInstance().refresh(true)
    }
}
