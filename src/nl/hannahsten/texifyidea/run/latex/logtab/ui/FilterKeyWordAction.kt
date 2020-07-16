package nl.hannahsten.texifyidea.run.latex.logtab.ui

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project

class FilterKeyWordAction(val keyword: String, val project: Project?) : ToggleAction("Hide $keyword messages"),
    DumbAware {
    private fun config(): LatexErrorTreeViewConfiguration {
        if (project == null) throw NullPointerException("What the hell?")
        else return LatexErrorTreeViewConfiguration.getInstance(
            project
        )
    }

    override fun isSelected(e: AnActionEvent): Boolean = config().showOverfullHBox

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        config().showOverfullHBox = state
    }
}