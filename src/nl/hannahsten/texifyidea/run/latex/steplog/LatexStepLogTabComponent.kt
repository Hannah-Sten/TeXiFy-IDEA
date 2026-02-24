package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.diagnostic.logging.AdditionalTabComponent
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.run.latex.flow.LatexStepExecution
import nl.hannahsten.texifyidea.run.latex.flow.StepAwareSequentialProcessHandler
import nl.hannahsten.texifyidea.run.latex.flow.StepLogEvent
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

internal class LatexStepLogTabComponent(
    private val project: Project,
    mainFile: VirtualFile?,
    private val handler: StepAwareSequentialProcessHandler,
) : AdditionalTabComponent(BorderLayout()), TreeSelectionListener {

    private val runNodeData = RunNodeData(
        title = mainFile?.nameWithoutExtension ?: "Run",
        status = NodeStatus.UNKNOWN,
    )
    private val rootNode = DefaultMutableTreeNode(runNodeData)
    private val treeModel = DefaultTreeModel(rootNode)
    private val tree = Tree(treeModel).apply {
        isRootVisible = true
        showsRootHandles = true
        cellRenderer = StepTreeCellRenderer()
        addTreeSelectionListener(this@LatexStepLogTabComponent)
    }
    private val rawLogArea = JTextArea().apply {
        isEditable = false
        lineWrap = false
        font = Font(Font.MONOSPACED, Font.PLAIN, JBUI.scale(14))
    }

    private val stepNodes = linkedMapOf<Int, DefaultMutableTreeNode>()
    private val stepNodeData = linkedMapOf<Int, StepNodeData>()
    private val parsers = linkedMapOf<Int, StepMessageParserSession>()
    private val stepHasWarnings = mutableSetOf<Int>()
    private val stepHasErrors = mutableSetOf<Int>()

    private var runExitCode: Int? = null
    private var currentStepIndex: Int? = null

    init {
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JBScrollPane(tree), JBScrollPane(rawLogArea)).apply {
            resizeWeight = 0.5
        }
        add(splitPane, BorderLayout.CENTER)

        initializeSteps(handler.executions, mainFile)
        tree.expandPath(TreePath(rootNode.path))

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
            val app = ApplicationManager.getApplication()
            if (app.isDispatchThread) {
                handleEvent(event)
            }
            else {
                app.invokeAndWait {
                    handleEvent(event)
                }
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
        val mainFileName = mainFile?.name
        executions.sortedBy { it.index }.forEach { execution ->
            val nodeData = StepNodeData(
                execution = execution,
                status = NodeStatus.UNKNOWN,
                fileName = mainFileName,
            )
            val stepNode = DefaultMutableTreeNode(nodeData)
            rootNode.add(stepNode)
            stepNodes[execution.index] = stepNode
            stepNodeData[execution.index] = nodeData
            parsers[execution.index] = StepMessageParserFactory.create(execution.type, mainFile)
        }
    }

    private fun handleEvent(event: StepLogEvent) {
        when (event) {
            is StepLogEvent.StepStarted -> {
                currentStepIndex = event.execution.index
                updateStepStatus(event.execution.index, NodeStatus.RUNNING)
                updateRunStatus()
                selectStep(event.execution.index)
            }

            is StepLogEvent.StepOutput -> {
                val parser = parsers[event.execution.index] ?: return
                parser.onText(event.text).forEach { parsedMessage ->
                    addParsedMessage(event.execution.index, parsedMessage)
                }
                refreshRawLogView()
            }

            is StepLogEvent.StepFinished -> {
                val status = when {
                    event.exitCode != 0 -> NodeStatus.FAILED
                    event.execution.index in stepHasErrors -> NodeStatus.FAILED
                    event.execution.index in stepHasWarnings -> NodeStatus.WARNING
                    else -> NodeStatus.SUCCEEDED
                }
                updateStepStatus(event.execution.index, status)
                updateRunStatus()
            }

            is StepLogEvent.RunFinished -> {
                runExitCode = event.exitCode
                if (event.exitCode != 0) {
                    stepNodeData.forEach { (index, data) ->
                        if (data.status == NodeStatus.UNKNOWN) {
                            updateStepStatus(index, NodeStatus.SKIPPED)
                        }
                    }
                }
                updateRunStatus()
                refreshRawLogView()
            }
        }
    }

    private fun addParsedMessage(stepIndex: Int, message: ParsedStepMessage) {
        when (message.level) {
            ParsedStepMessageLevel.ERROR -> stepHasErrors += stepIndex
            ParsedStepMessageLevel.WARNING -> stepHasWarnings += stepIndex
        }

        val stepNode = stepNodes[stepIndex] ?: return
        stepNode.add(DefaultMutableTreeNode(MessageNodeData(message)))
        treeModel.nodeStructureChanged(stepNode)
        tree.expandPath(TreePath(stepNode.path))
    }

    private fun selectStep(stepIndex: Int) {
        val node = stepNodes[stepIndex] ?: return
        val path = TreePath(node.path)
        tree.selectionPath = path
        tree.scrollPathToVisible(path)
    }

    private fun updateStepStatus(stepIndex: Int, status: NodeStatus) {
        val data = stepNodeData[stepIndex] ?: return
        if (data.status == status) {
            return
        }
        data.status = status
        treeModel.nodeChanged(stepNodes[stepIndex])
    }

    private fun updateRunStatus() {
        runNodeData.status = when {
            runExitCode != null && runExitCode != 0 -> NodeStatus.FAILED
            stepNodeData.values.any { it.status == NodeStatus.RUNNING } -> NodeStatus.RUNNING
            runExitCode != null && stepNodeData.values.any { it.status == NodeStatus.WARNING } -> NodeStatus.WARNING
            runExitCode != null && stepNodeData.values.any { it.status == NodeStatus.FAILED } -> NodeStatus.FAILED
            runExitCode != null -> NodeStatus.SUCCEEDED
            else -> NodeStatus.UNKNOWN
        }
        treeModel.nodeChanged(rootNode)
    }

    private fun refreshRawLogView() {
        val selectedStepIndex = selectedStepIndex()
        if (selectedStepIndex == null) {
            rawLogArea.text = ""
            return
        }

        rawLogArea.text = handler.rawLog(selectedStepIndex)
        if (selectedStepIndex == currentStepIndex) {
            rawLogArea.caretPosition = rawLogArea.document.length
        }
    }

    private fun selectedStepIndex(): Int? {
        val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return currentStepIndex ?: stepNodes.keys.firstOrNull()
        return when (val userObject = node.userObject) {
            is StepNodeData -> userObject.execution.index
            is MessageNodeData -> ((node.parent as? DefaultMutableTreeNode)?.userObject as? StepNodeData)?.execution?.index
            is RunNodeData -> currentStepIndex ?: stepNodes.keys.firstOrNull()
            else -> currentStepIndex ?: stepNodes.keys.firstOrNull()
        }
    }

    private enum class NodeStatus {
        UNKNOWN,
        RUNNING,
        SUCCEEDED,
        SKIPPED,
        WARNING,
        FAILED,
    }

    private data class RunNodeData(
        val title: String,
        var status: NodeStatus,
    )

    private data class StepNodeData(
        val execution: LatexStepExecution,
        var status: NodeStatus,
        val fileName: String?,
    )

    private data class MessageNodeData(
        val message: ParsedStepMessage,
    )

    private inner class StepTreeCellRenderer : ColoredTreeCellRenderer() {

        override fun customizeCellRenderer(
            tree: JTree,
            value: Any,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean,
        ) {
            val node = value as? DefaultMutableTreeNode ?: return
            when (val data = node.userObject) {
                is RunNodeData -> {
                    icon = data.status.icon()
                    append("${data.title}: ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    append(data.status.presentableText(), SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }

                is StepNodeData -> {
                    icon = data.status.icon()
                    append(data.execution.displayName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    data.fileName?.let { append(" $it", SimpleTextAttributes.GRAYED_ATTRIBUTES) }
                }

                is MessageNodeData -> {
                    icon = when (data.message.level) {
                        ParsedStepMessageLevel.ERROR -> AllIcons.RunConfigurations.TestError
                        ParsedStepMessageLevel.WARNING -> AllIcons.General.Warning
                    }
                    append(data.message.message, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    messageLocation(data.message)?.let { location ->
                        append(" $location", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    }
                }

                else -> {
                    icon = EmptyIcon.ICON_16
                    append(data?.toString().orEmpty(), SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }
            }
        }

        private fun messageLocation(message: ParsedStepMessage): String? {
            val raw = message.file?.name ?: message.fileName ?: return null
            val fileName = raw.substringAfterLast('/').substringAfterLast('\\')
            return if (fileName.isBlank()) null else fileName
        }
    }

    private fun NodeStatus.presentableText(): String = when (this) {
        NodeStatus.UNKNOWN -> "Pending"
        NodeStatus.RUNNING -> "Running"
        NodeStatus.SUCCEEDED -> "Successful"
        NodeStatus.SKIPPED -> "Skipped"
        NodeStatus.WARNING -> "Completed with warnings"
        NodeStatus.FAILED -> "Failed"
    }

    private fun NodeStatus.icon() = when (this) {
        NodeStatus.UNKNOWN -> EmptyIcon.ICON_16
        NodeStatus.RUNNING -> AnimatedIcon.Default()
        NodeStatus.SUCCEEDED -> AllIcons.RunConfigurations.TestPassed
        NodeStatus.SKIPPED -> AllIcons.RunConfigurations.TestIgnored
        NodeStatus.WARNING -> AllIcons.General.Warning
        NodeStatus.FAILED -> AllIcons.RunConfigurations.TestError
    }
}
