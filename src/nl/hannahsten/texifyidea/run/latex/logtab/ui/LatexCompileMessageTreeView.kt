package nl.hannahsten.texifyidea.run.latex.logtab.ui

import com.intellij.ide.IdeBundle
import com.intellij.ide.errorTreeView.ErrorTreeElement
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage

class LatexCompileMessageTreeView(val project: Project, val logMessages: MutableList<LatexLogMessage>) : NewErrorTreeViewPanel(project, null) {
    fun config(): LatexErrorTreeViewConfiguration = LatexErrorTreeViewConfiguration.getInstance(myProject)
    // Function so it is not null in fillRightToolBarGroup

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

    fun getAllElements(): Set<ErrorTreeElement> {
        this@LatexCompileMessageTreeView.errorViewStructure.let {tree ->
            return tree.getChildElements(tree.rootElement).flatMap {
                tree.getChildElements(it).toList()
            }.toSet()
        }
    }

    inner class FilterKeyWordAction(val keyword: String, val project: Project) : ToggleAction("Hide $keyword messages"), DumbAware {
        override fun isSelected(e: AnActionEvent): Boolean = config().showOverfullHBox

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            val treeView = this@LatexCompileMessageTreeView
            config().showOverfullHBox = state
            if (state) {
                val messagesToUnhide = treeView.logMessages.filter { it.message.toLowerCase().contains(keyword.toLowerCase()) }
                messagesToUnhide.forEach { addMessage(it) }
            }
            else {
                val messagesToHide = getAllElements().filter { it.text.joinToString("").toLowerCase().contains(keyword.toLowerCase()) }
                messagesToHide.forEach {
                    treeView.errorViewStructure.removeElement(it)
                }
            }
            treeView.updateTree()
        }
    }

    fun addMessage(message: LatexLogMessage) {
        // Correct the index because the treeview starts counting at line 0 instead of line 1.
        addMessage(message.type.category, arrayOf(message.message), message.file, message.line - 1, -1, null)
    }
}

