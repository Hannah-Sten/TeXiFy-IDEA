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
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.containsAny
import javax.swing.Icon

class LatexCompileMessageTreeView(val project: Project, val logMessages: MutableList<LatexLogMessage>) :
    NewErrorTreeViewPanel(project, null) {
    fun config(): LatexErrorTreeViewConfiguration = LatexErrorTreeViewConfiguration.getInstance(myProject)

    override fun fillRightToolbarGroup(group: DefaultActionGroup) {
        // Use myProject (from NewErrorTreeViewPanel) because somehow project is null
        Magic.Icon.logKeywordFilterIcons.forEach { group.add(FilterKeyWordAction(it.key, it.value, myProject)) }
        // Search for the information action toggle so we can remove it (as we don't use it).
        val informationAction = group.childActionsOrStubs.find {
            it.templateText?.contains(IdeBundle.messagePointer("action.show.infos").get()) == true
        }
        if (informationAction != null) {
            group.remove(informationAction)
        }
        super.fillRightToolbarGroup(group)
    }

    /**
     * Get all elements that are currently in the tree.
     *
     * Elements are located as direct children of a file, which are direct children of the root.
     * Therefor, we only have to look one level deep to get all the messages.
     */
    fun getAllElements(): Set<ErrorTreeElement> {
        this@LatexCompileMessageTreeView.errorViewStructure.let { tree ->
            return tree.getChildElements(tree.rootElement).flatMap {
                tree.getChildElements(it).toList()
            }.toSet()
        }
    }

    fun addMessage(message: LatexLogMessage) {
        // Correct the index because the tree view starts counting at line 0 instead of line 1.
        addMessage(message.type.category, arrayOf(message.message), message.file, message.line - 1, -1, null)
    }

    fun applyFilters() {
        logMessages.forEach {
            val hide = it.message.toLowerCase().containsAny(config().showKeywordWarnings.filter { (_, v) -> !v }.keys)
            if (!hide && it !in this) {
                addMessage(it)
            }
            if (hide && it in this) {
                errorViewStructure.removeElement(
                    // This element exists because we have checked for it with `it in this`
                    getAllElements().first { e -> e.text.joinToString() == it.message }
                )
            }
        }
        updateTree()
    }

    private operator fun contains(message: LatexLogMessage): Boolean =
        getAllElements().map { it.text.joinToString() }.contains(message.message)

    inner class FilterKeyWordAction(private val keyword: String, val icon: Icon, val project: Project) : ToggleAction("text", "Hide $keyword messages", icon), DumbAware {
        override fun isSelected(e: AnActionEvent): Boolean = config().showKeywordWarnings[keyword] ?: true

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            config().showKeywordWarnings[keyword] = state
            applyFilters()
        }
    }
}
