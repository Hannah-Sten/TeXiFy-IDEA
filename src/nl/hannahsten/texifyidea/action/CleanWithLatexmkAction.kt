package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCleanUtil
import nl.hannahsten.texifyidea.run.latexmk.LatexmkRunConfiguration
import nl.hannahsten.texifyidea.util.selectedRunConfig

class CleanWithLatexmkAction : AnAction(), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project?.selectedRunConfig() is LatexmkRunConfiguration
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val runConfig = project.selectedRunConfig() as? LatexmkRunConfiguration ?: return
        val mode = LatexmkCleanUtil.promptMode(project) ?: return
        LatexmkCleanUtil.run(project, runConfig, mode)
    }
}
