package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.diagnostic.logging.AdditionalTabComponent
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import nl.hannahsten.texifyidea.run.latex.flow.LatexStepExecution
import nl.hannahsten.texifyidea.run.latex.flow.StepAwareSequentialProcessHandler
import nl.hannahsten.texifyidea.run.latex.flow.StepLogEvent
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel

internal class LatexStepLogTabComponent(
    private val project: Project,
    mainFile: VirtualFile?,
    private val handler: StepAwareSequentialProcessHandler,
) : AdditionalTabComponent(BorderLayout()), TreeSelectionListener {

    private val rootNode = DefaultMutableTreeNode("Run")
    private val treeModel = DefaultTreeModel(rootNode)
    private val tree = Tree(treeModel).apply {
        isRootVisible = true
        cellRenderer = StepTreeCellRenderer()
        addTreeSelectionListener(this@LatexStepLogTabComponent)
    }
    private val rawLogArea = JTextArea().apply {
        isEditable = false
        lineWrap = false
    }

    private val stepNodes = linkedMapOf<Int, DefaultMutableTreeNode>()
    private val stepNodeData = linkedMapOf<Int, StepNodeData>()
    private val unsupportedPlaceholders = linkedMapOf<Int, DefaultMutableTreeNode>()
    private val parsers = linkedMapOf<Int, StepMessageParserSession>()

    init {
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JBScrollPane(tree), JBScrollPane(rawLogArea)).apply {
            resizeWeight = 0.5
        }
        add(splitPane, BorderLayout.CENTER)

        initializeSteps(handler.executions, mainFile)
        tree.expandRow(0)

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount != 2) {
                    return
                }
                val path = tree.getPathForLocation(e.x, e.y) ?: return
                val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
                val data = node.userObject as? MessageNodeData ?: return
                val file = data.message.file ?: return
                val line = (data.message.line ?: 1).coerceAtLeast(1) - 1
                OpenFileDescriptor(project, file, line, 0).navigate(true)
            }
        })

        handler.addStepLogListener { event ->
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                handleEvent(event)
            }
        }
    }

    override fun getTabTitle(): String = "Step Log"

    override fun dispose() {
    }

    override fun getPreferredFocusableComponent(): JComponent = tree

    override fun getToolbarActions(): ActionGroup? = null

    override fun getToolbarContextComponent(): JComponent? = null

    override fun getToolbarPlace(): String = "top"

    override fun getSearchComponent(): JComponent? = null

    override fun isContentBuiltIn(): Boolean = false

    override fun valueChanged(e: TreeSelectionEvent?) {
        refreshRawLogView()
    }

    internal fun stepStatus(index: Int): String? = stepNodeData[index]?.status?.name

    internal fun parsedMessageCount(index: Int): Int {
        val stepNode = stepNodes[index] ?: return 0
        return (0 until stepNode.childCount)
            .map { stepNode.getChildAt(it) as DefaultMutableTreeNode }
            .count { it.userObject is MessageNodeData }
    }

    private fun initializeSteps(executions: List<LatexStepExecution>, mainFile: VirtualFile?) {
        executions.sortedBy { it.index }.forEach { execution ->
            val nodeData = StepNodeData(execution = execution, status = StepStatus.PENDING)
            val stepNode = DefaultMutableTreeNode(nodeData)
            rootNode.add(stepNode)
            stepNodes[execution.index] = stepNode
            stepNodeData[execution.index] = nodeData

            val parser = StepMessageParserFactory.create(execution.type, mainFile)
            parsers[execution.index] = parser
            if (!parser.supportsStructuredMessages) {
                val placeholder = DefaultMutableTreeNode("No structured messages (raw log only).")
                stepNode.add(placeholder)
                unsupportedPlaceholders[execution.index] = placeholder
            }
        }
    }

    private fun handleEvent(event: StepLogEvent) {
        when (event) {
            is StepLogEvent.StepStarted -> {
                updateStepStatus(event.execution.index, StepStatus.RUNNING)
            }

            is StepLogEvent.StepOutput -> {
                val parser = parsers[event.execution.index] ?: return
                val parsed = parser.onText(event.text)
                parsed.forEach { parsedMessage ->
                    addParsedMessage(event.execution.index, parsedMessage)
                }
                refreshRawLogView()
            }

            is StepLogEvent.StepFinished -> {
                val status = if (event.exitCode == 0) StepStatus.SUCCESS else StepStatus.FAILED
                updateStepStatus(event.execution.index, status)
            }

            is StepLogEvent.RunFinished -> {
                if (event.exitCode != 0) {
                    stepNodeData.forEach { (index, data) ->
                        if (data.status == StepStatus.PENDING) {
                            updateStepStatus(index, StepStatus.SKIPPED)
                        }
                    }
                }
                refreshRawLogView()
            }
        }
    }

    private fun addParsedMessage(stepIndex: Int, message: ParsedStepMessage) {
        val stepNode = stepNodes[stepIndex] ?: return
        unsupportedPlaceholders.remove(stepIndex)?.let(stepNode::remove)
        stepNode.add(DefaultMutableTreeNode(MessageNodeData(message)))
        treeModel.nodeStructureChanged(stepNode)
        tree.expandPath(javax.swing.tree.TreePath(stepNode.path))
    }

    private fun updateStepStatus(stepIndex: Int, status: StepStatus) {
        val data = stepNodeData[stepIndex] ?: return
        if (data.status == status) {
            return
        }
        data.status = status
        treeModel.nodeChanged(stepNodes[stepIndex])
    }

    private fun refreshRawLogView() {
        val selectedStepIndex = selectedStepIndex() ?: return
        rawLogArea.text = handler.rawLog(selectedStepIndex)
        rawLogArea.caretPosition = rawLogArea.document.length
    }

    private fun selectedStepIndex(): Int? {
        val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return stepNodes.keys.firstOrNull()
        val userObject = node.userObject
        return when (userObject) {
            is StepNodeData -> userObject.execution.index
            is MessageNodeData -> {
                val parentData = (node.parent as? DefaultMutableTreeNode)?.userObject as? StepNodeData
                parentData?.execution?.index
            }

            else -> stepNodes.keys.firstOrNull()
        }
    }

    private enum class StepStatus {
        PENDING,
        RUNNING,
        SUCCESS,
        FAILED,
        SKIPPED,
    }

    private data class StepNodeData(
        val execution: LatexStepExecution,
        var status: StepStatus,
    ) {

        override fun toString(): String = "${execution.index + 1}. ${execution.displayName} (${status.name.lowercase()})"
    }

    private data class MessageNodeData(
        val message: ParsedStepMessage,
    ) {

        override fun toString(): String = "${message.level.name}: ${message.message}"
    }

    private class StepTreeCellRenderer : DefaultTreeCellRenderer() {

        override fun getTreeCellRendererComponent(
            tree: JTree,
            value: Any,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ): Component {
            val component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
            val node = value as? DefaultMutableTreeNode ?: return component
            when (val data = node.userObject) {
                is StepNodeData -> {
                    icon = when (data.status) {
                        StepStatus.RUNNING -> AllIcons.General.Information
                        StepStatus.SUCCESS -> AllIcons.General.Information
                        StepStatus.FAILED -> AllIcons.General.ErrorDialog
                        StepStatus.SKIPPED -> AllIcons.General.WarningDialog
                        StepStatus.PENDING -> null
                    }
                }

                is MessageNodeData -> {
                    icon = when (data.message.level) {
                        ParsedStepMessageLevel.ERROR -> AllIcons.General.ErrorDialog
                        ParsedStepMessageLevel.WARNING -> AllIcons.General.Warning
                    }
                }

                else -> {
                    icon = null
                }
            }
            return component
        }
    }
}
