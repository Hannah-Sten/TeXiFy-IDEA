package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.ide.IdeBundle
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project

class LatexCompileMessageTreeView(project: Project) : NewErrorTreeViewPanel(project, null) {
    override fun fillRightToolbarGroup(group: DefaultActionGroup) {
        group.add(DummyAction())
        // Search for the information action toggle so we can remove it (as we don't use it).
        val informationAction = group.childActionsOrStubs.find { it.templateText?.contains(IdeBundle.messagePointer("action.show.infos").get()) == true }
        if (informationAction != null) {
            group.remove(informationAction)
        }
        super.fillRightToolbarGroup(group)
    }

    class DummyAction : ToggleAction("Test action"), DumbAware {
        override fun isSelected(e: AnActionEvent): Boolean = true

        override fun setSelected(e: AnActionEvent, state: Boolean) {
        }
    }
}
