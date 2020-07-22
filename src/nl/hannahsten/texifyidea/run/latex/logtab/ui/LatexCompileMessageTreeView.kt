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
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.remove

class LatexCompileMessageTreeView(val project: Project, val logMessages: MutableList<LatexLogMessage>) :
    NewErrorTreeViewPanel(project, null) {
    fun config(): LatexErrorTreeViewConfiguration = LatexErrorTreeViewConfiguration.getInstance(myProject)

    override fun fillRightToolbarGroup(group: DefaultActionGroup) {
        // Use myProject (from NewErrorTreeViewPanel) because somehow project is null
        LatexKeywordFilters.values().forEach { group.add(FilterKeywordAction(it, myProject)) }
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
    private fun getAllElements(): Set<ErrorTreeElement> {
        this@LatexCompileMessageTreeView.errorViewStructure.let { tree ->
            return tree.getChildElements(tree.rootElement).flatMap {
                tree.getChildElements(it).toList()
            }.toSet()
        }
    }

    /**
     * Add this [message] to the tree view.
     */
    private fun addMessage(message: LatexLogMessage) {
        // Correct the index because the tree view starts counting at line 0 instead of line 1.
        addMessage(message.type.category, arrayOf(message.message), message.file, message.line - 1, -1, null)
    }

    /**
     * Apply all filters to all messages in the [logMessages] list.
     */
    fun applyFilters() {
        logMessages.forEach {
            applyFilters(it)
        }
    }

    fun applyFilters(logMessage: LatexLogMessage) {
        val hide = logMessage.message.toLowerCase().containsAny(
            LatexKeywordFilters.values()
                .filter { f -> config().showKeywordWarnings[f]?.not() ?: false }
                .map { f -> f.triggers }.flatten().toSet()
        )
        if (!hide && logMessage !in this) {
            addMessage(logMessage)
        }
        if (hide && logMessage in this) {
            errorViewStructure.removeElement(
                // This element exists because we have checked for it with `logMessage in this`
                getAllElements().first { e -> e.fullString() == logMessage.toTreeViewString() }
            )
        }
        updateTree()
    }

    // Remove , because they are inserted for line numbers > 999
    private fun ErrorTreeElement.fullString(): String = "${exportTextPrefix.remove(",")} ${text.joinToString()}"

    /**
     * Check if a message is currently in the tree.
     */
    private operator fun contains(message: LatexLogMessage): Boolean =
        getAllElements().map { it.fullString() }.contains(message.toTreeViewString())

    inner class FilterKeywordAction(private val keyword: LatexKeywordFilters, val project: Project) : ToggleAction("Show $keyword messages", "Show $keyword messages", keyword.icon), DumbAware {
        override fun isSelected(e: AnActionEvent): Boolean = config().showKeywordWarnings[keyword] ?: true

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            config().showKeywordWarnings[keyword] = state
            applyFilters()
        }
    }
}
