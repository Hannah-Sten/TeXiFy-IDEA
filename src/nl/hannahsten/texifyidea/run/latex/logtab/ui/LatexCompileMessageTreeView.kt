package nl.hannahsten.texifyidea.run.latex.logtab.ui

import com.intellij.ide.IdeBundle
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project

class LatexCompileMessageTreeView(project: Project) : NewErrorTreeViewPanel(project, null) {
    override fun fillRightToolbarGroup(group: DefaultActionGroup) {
        // Use myProject (from NewErrorTreeViewPanel) because somehow project is null
        group.add(FilterKeyWordAction("overfull \\hbox", myProject))
        // Search for the information action toggle so we can remove it (as we don't use it).
        val informationAction = group.childActionsOrStubs.find { it.templateText?.contains(IdeBundle.messagePointer("action.show.infos").get()) == true }
        if (informationAction != null) {
            group.remove(informationAction)
        }
        super.fillRightToolbarGroup(group)
    }
}

