package nl.hannahsten.texifyidea.run.latex.logtab.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.ide.errorTreeView.ErrorTreeElement
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.remove
import java.util.*

class LatexCompileMessageTreeView(
    val project: Project,
    val latexMessageList: MutableList<LatexLogMessage>,
    val bibtexMessageList: MutableList<BibtexLogMessage>
) :
    NewErrorTreeViewPanel(project, null) {

    fun config(): LatexErrorTreeViewConfiguration = LatexErrorTreeViewConfiguration.getInstance(myProject)

    override fun fillRightToolbarGroup(group: DefaultActionGroup) {
        // Use myProject (from NewErrorTreeViewPanel) because somehow project is null
        LatexKeywordFilter.values().forEach { group.add(FilterKeywordAction(it, myProject)) }
        group.addAll(FilterBibtexAction(myProject), ExpandAllAction(), CollapseAllAction())

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
     * Therefore, we only have to look one level deep to get all the messages.
     */
    private fun getAllElements(): List<ErrorTreeElement> {
        this@LatexCompileMessageTreeView.errorViewStructure.let { tree ->
            return tree.getChildElements(tree.rootElement).flatMap {
                tree.getChildElements(it).toList()
            }
        }
    }

    /**
     * Get selected elements.
     */
    private fun getSelectedElements(): List<ErrorTreeElement> {
        val selectedRows = myTree.selectionRows ?: return emptyList()
        val tree = errorViewStructure
        val rows = tree.getChildElements(tree.rootElement).flatMap {
            listOf(it) + tree.getChildElements(it).toList()
        }
        return rows.filterIndexed { index, _ -> index in selectedRows }
    }

    /**
     * Add this [message] to the tree view.
     */
    private fun addMessage(message: LatexLogMessage) {
        // Correct the index because the tree view starts counting at line 0 instead of line 1.
        addMessage(message.type.category, arrayOf(message.message), message.file, message.line - 1, -1, null)
    }

    private fun addMessage(logMessage: BibtexLogMessage) {
        addMessage(
            logMessage.type.category, arrayOf(logMessage.message), logMessage.file,
            // Treeview starts at 0
            (logMessage.line ?: 0) - 1, -1, null
        )
    }

    /**
     * Apply all filters to all messages in the [latexMessageList] list.
     */
    fun applyFiltersToAllMessages() {
        latexMessageList.forEach { applyFilters(it) }
        bibtexMessageList.forEach { applyFilters(it) }
    }

    /**
     * Apply all filters to [logMessage].
     */
    fun applyFilters(logMessage: LatexLogMessage) {
        val hide = logMessage.message.lowercase(Locale.getDefault()).containsAny(
            LatexKeywordFilter.values()
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

    fun applyFilters(logMessage: BibtexLogMessage) {
        if (config().showBibtexWarnings && logMessage !in this) {
            addMessage(logMessage)
        }
        if (!config().showBibtexWarnings && logMessage in this) {
            errorViewStructure.removeElement(
                getAllElements().first { e -> e.fullString() == logMessage.toTreeViewString() }
            )
        }
    }

    // Remove commas, because they are inserted for line numbers > 999
    private fun ErrorTreeElement.fullString(): String = "${exportTextPrefix.remove(",")} ${text.joinToString()}"

    /**
     * Check if a message is currently in the tree.
     */
    private operator fun contains(message: LatexLogMessage): Boolean =
        getAllElements().map { it.fullString() }.contains(message.toTreeViewString())

    private operator fun contains(message: BibtexLogMessage): Boolean =
        getAllElements().map { it.fullString() }.contains(message.toTreeViewString())

    inner class FilterKeywordAction(private val keyword: LatexKeywordFilter, val project: Project) :
        ToggleAction("Show $keyword messages", "Show $keyword messages", keyword.icon), DumbAware {

        override fun isSelected(e: AnActionEvent): Boolean = config().showKeywordWarnings[keyword] ?: true

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            config().showKeywordWarnings[keyword] = state
            applyFiltersToAllMessages()
        }
    }

    inner class FilterBibtexAction(val project: Project) :
        ToggleAction("Show Bibtex Messages by Latexmk", "", TexifyIcons.DOT_BIB), DumbAware {

        override fun isSelected(e: AnActionEvent): Boolean = config().showBibtexWarnings

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            config().showBibtexWarnings = state
            applyFiltersToAllMessages()
        }
    }

    inner class ExpandAllAction : AnAction("Expand All", "", AllIcons.Actions.Expandall), DumbAware {

        override fun actionPerformed(e: AnActionEvent) {
            expandAll()
            config().expanded = true
        }
    }

    inner class CollapseAllAction : AnAction("Collapse All", "", AllIcons.Actions.Collapseall), DumbAware {

        override fun actionPerformed(e: AnActionEvent) {
            collapseAll()
            config().expanded = false
        }
    }
}
