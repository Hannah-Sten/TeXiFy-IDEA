package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.diagnostic.logging.AdditionalTabComponent
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.tree.TreeUtil
import nl.hannahsten.texifyidea.run.latex.flow.StepAwareSequentialProcessHandler
import nl.hannahsten.texifyidea.run.latex.flow.StepLogEvent
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStep
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

internal class LatexStepLogTabComponent(
    private val project: Project,
    mainFile: VirtualFile?,
    handler: StepAwareSequentialProcessHandler,
) : AdditionalTabComponent(BorderLayout()), ExecutionConsole, TreeSelectionListener {

    companion object {

        private const val SPLITTER_PROPORTION_PROPERTY = "TeXiFy.StepLog.Splitter.Proportion"
    }

    private val runNodeData = RunNodeData(
        title = mainFile?.nameWithoutExtension ?: "Run",
        status = NodeStatus.UNKNOWN,
    )

    private val rootNode = StepTreeNode(parent = null, runData = runNodeData)
    private val treeStructure = StepTreeStructure()
    private val structureTreeModel = StructureTreeModel(treeStructure, this)
    private val treeModel = AsyncTreeModel(structureTreeModel, this)
    private val tree = Tree(treeModel).apply {
        isRootVisible = true
        showsRootHandles = true
        addTreeSelectionListener(this@LatexStepLogTabComponent)
    }

    private val console: ConsoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
    private val consoleToolbarGroup = DefaultActionGroup(console.createConsoleActions().toList())
    private val consoleToolbar = ActionManager.getInstance().createActionToolbar(
        "TeXiFy.StepLog.ConsoleToolbar",
        consoleToolbarGroup,
        true,
    ).apply {
        targetComponent = console.component
    }
    private val consolePanel = JPanel(BorderLayout()).apply {
        add(consoleToolbar.component, BorderLayout.NORTH)
        add(console.component, BorderLayout.CENTER)
    }

    private val splitter = OnePixelSplitter(SPLITTER_PROPORTION_PROPERTY, 0.5f).apply {
        firstComponent = JBScrollPane(tree)
        secondComponent = consolePanel
    }

    private val stepNodes = linkedMapOf<Int, StepTreeNode>()
    private val stepNodeData = linkedMapOf<Int, StepNodeData>()
    private val parsers = linkedMapOf<Int, StepMessageParserSession>()
    private val stepOutputChunks = linkedMapOf<Int, MutableList<StepOutputChunk>>()
    private val stepHasWarnings = mutableSetOf<Int>()
    private val stepHasErrors = mutableSetOf<Int>()

    private var runExitCode: Int? = null
    private var currentStepIndex: Int? = null
    private var renderedStepIndex: Int? = null
    private var renderedOutputText: String = ""

    init {
        add(splitter, BorderLayout.CENTER)

        initializeSteps(handler.steps, mainFile)
        scheduleUpdate(rootNode, structureChanged = true)
        TreeUtil.expand(tree, 2)

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount != 2) {
                    return
                }
                val path = tree.getPathForLocation(e.x, e.y) ?: return
                val node = extractTreeNode(path.lastPathComponent) ?: return
                val data = node.messageData ?: return
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
        console.dispose()
    }

    override fun getPreferredFocusableComponent(): JComponent = tree

    // Console toolbar is embedded in the right pane, so keep additional-tab toolbar empty.
    override fun getToolbarActions(): ActionGroup? = null

    override fun getToolbarContextComponent(): JComponent? = null

    override fun getToolbarPlace(): String = "top"

    override fun getSearchComponent(): JComponent? = null

    override fun isContentBuiltIn(): Boolean = false

    override fun valueChanged(e: TreeSelectionEvent?) {
        refreshRawLogView()
    }

    private fun initializeSteps(steps: List<LatexRunStep>, mainFile: VirtualFile?) {
        val mainFileName = mainFile?.name
        steps.forEachIndexed { index, step ->
            val nodeData = StepNodeData(
                index = index,
                step = step,
                status = NodeStatus.UNKNOWN,
                fileName = mainFileName,
            )
            val stepNode = StepTreeNode(parent = rootNode, stepData = nodeData)
            rootNode.children.add(stepNode)
            stepNodes[index] = stepNode
            stepNodeData[index] = nodeData
            parsers[index] = StepMessageParserFactory.create(step.id, mainFile)
            stepOutputChunks[index] = mutableListOf()
        }
    }

    private fun handleEvent(event: StepLogEvent) {
        when (event) {
            is StepLogEvent.StepStarted -> {
                currentStepIndex = event.index
                updateStepStatus(event.index, NodeStatus.RUNNING)
                updateRunStatus()
                selectStep(event.index)
            }

            is StepLogEvent.StepOutput -> {
                val chunk = StepOutputChunk(event.text, event.outputType)
                stepOutputChunks[event.index]?.add(chunk)
                when (val selection = selectedRawLogSelection()) {
                    is RawLogSelection.Step -> {
                        if (selection.stepIndex == event.index) {
                            printChunk(chunk)
                            renderedStepIndex = event.index
                            renderedOutputText += event.text
                        }
                    }

                    RawLogSelection.All -> {
                        printChunk(chunk)
                        renderedStepIndex = null
                        renderedOutputText += event.text
                    }

                    RawLogSelection.None -> {
                    }
                }

                val parser = parsers[event.index] ?: return
                parser.onText(event.text).forEach { parsedMessage ->
                    addParsedMessage(event.index, parsedMessage)
                }
            }

            is StepLogEvent.StepFinished -> {
                val status = when {
                    event.exitCode != 0 -> NodeStatus.FAILED
                    event.index in stepHasErrors -> NodeStatus.FAILED
                    event.index in stepHasWarnings -> NodeStatus.WARNING
                    else -> NodeStatus.SUCCEEDED
                }
                updateStepStatus(event.index, status)
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
        stepNode.children.add(StepTreeNode(parent = stepNode, messageData = MessageNodeData(message)))
        scheduleUpdate(stepNode, structureChanged = true)
        TreeUtil.expand(tree, 3)
    }

    private fun selectStep(stepIndex: Int) {
        val node = stepNodes[stepIndex] ?: return
        treePathFor(node)?.let { path ->
            tree.selectionPath = path
            tree.scrollPathToVisible(path)
        }
    }

    private fun updateStepStatus(stepIndex: Int, status: NodeStatus) {
        val data = stepNodeData[stepIndex] ?: return
        if (data.status == status) {
            return
        }
        data.status = status
        stepNodes[stepIndex]?.let { scheduleUpdate(it) }
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
        scheduleUpdate(rootNode)
    }

    private fun refreshRawLogView() {
        when (val selection = selectedRawLogSelection()) {
            RawLogSelection.None -> {
                console.clear()
                renderedStepIndex = null
                renderedOutputText = ""
            }

            RawLogSelection.All -> {
                console.clear()
                val chunks = stepOutputChunks
                    .toSortedMap()
                    .values
                    .flatten()
                chunks.forEach { chunk -> printChunk(chunk) }
                renderedStepIndex = null
                renderedOutputText = chunks.joinToString(separator = "") { it.text }
            }

            is RawLogSelection.Step -> {
                console.clear()
                val chunks = stepOutputChunks[selection.stepIndex].orEmpty()
                chunks.forEach { chunk -> printChunk(chunk) }
                renderedStepIndex = selection.stepIndex
                renderedOutputText = chunks.joinToString(separator = "") { it.text }
            }
        }
    }

    private fun printChunk(chunk: StepOutputChunk) {
        val contentType = when (chunk.outputType) {
            ProcessOutputTypes.STDERR -> ConsoleViewContentType.ERROR_OUTPUT
            ProcessOutputTypes.SYSTEM -> ConsoleViewContentType.SYSTEM_OUTPUT
            else -> NORMAL_OUTPUT
        }
        console.print(chunk.text, contentType)
    }

    private fun selectedStepIndexForFallback(): Int? {
        if (currentStepIndex != null) {
            return currentStepIndex
        }
        return stepNodes.keys.firstOrNull()
    }

    private fun selectedRawLogSelection(): RawLogSelection {
        fun fallbackStepSelection(): RawLogSelection {
            val fallbackStepIndex = selectedStepIndexForFallback() ?: return RawLogSelection.None
            return RawLogSelection.Step(fallbackStepIndex)
        }

        val selectedNode = extractTreeNode(tree.lastSelectedPathComponent) ?: return fallbackStepSelection()
        return when {
            selectedNode.stepData != null -> RawLogSelection.Step(selectedNode.stepData.index)
            selectedNode.messageData != null -> selectedNode.parentStepIndex()?.let { RawLogSelection.Step(it) } ?: fallbackStepSelection()
            selectedNode.runData != null -> RawLogSelection.All
            else -> fallbackStepSelection()
        }
    }

    private fun scheduleUpdate(node: StepTreeNode, structureChanged: Boolean = false) {
        structureTreeModel.invalidate(node, structureChanged)
    }

    private fun treePathFor(node: StepTreeNode): TreePath? {
        val chain = mutableListOf<Any>()
        var current: StepTreeNode? = node
        while (current != null) {
            chain += current
            current = current.parentDescriptor as? StepTreeNode
        }
        if (chain.isEmpty()) {
            return null
        }
        return TreePath(chain.reversed().toTypedArray())
    }

    private fun extractTreeNode(component: Any?): StepTreeNode? = when (component) {
        is StepTreeNode -> component
        is DefaultMutableTreeNode -> component.userObject as? StepTreeNode
        // is NodeDescriptor<*> -> component as? StepTreeNode  // This cast is always null
        else -> null
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
        val index: Int,
        val step: LatexRunStep,
        var status: NodeStatus,
        val fileName: String?,
    )

    private data class MessageNodeData(
        val message: ParsedStepMessage,
    )

    private data class StepOutputChunk(
        val text: String,
        val outputType: Key<*>,
    )

    private sealed interface RawLogSelection {

        object None : RawLogSelection

        object All : RawLogSelection

        data class Step(
            val stepIndex: Int
        ) : RawLogSelection
    }

    private inner class StepTreeNode(
        parent: StepTreeNode?,
        val runData: RunNodeData? = null,
        val stepData: StepNodeData? = null,
        val messageData: MessageNodeData? = null,
    ) : PresentableNodeDescriptor<StepTreeNode>(project, parent) {

        val children = mutableListOf<StepTreeNode>()

        override fun getElement(): StepTreeNode = this

        override fun update(presentation: PresentationData) {
            presentation.clearText()
            when {
                runData != null -> {
                    presentation.setIcon(runData.status.icon())
                    presentation.addText("${runData.title}: ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    presentation.addText(runData.status.presentableText(), SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    myName = "${runData.title}: ${runData.status.presentableText()}"
                }

                stepData != null -> {
                    presentation.setIcon(stepData.status.icon())
                    presentation.addText(stepData.step.displayName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    stepData.fileName?.let { presentation.addText(" $it", SimpleTextAttributes.GRAYED_ATTRIBUTES) }
                    myName = stepData.step.displayName
                }

                messageData != null -> {
                    val msg = messageData.message
                    val icon = when (msg.level) {
                        ParsedStepMessageLevel.ERROR -> AllIcons.RunConfigurations.TestError
                        ParsedStepMessageLevel.WARNING -> AllIcons.General.Warning
                    }
                    presentation.setIcon(icon)
                    presentation.addText(msg.message, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    messageLocation(msg)?.let { location ->
                        presentation.addText(" $location", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    }
                    myName = msg.message
                }

                else -> {
                    presentation.setIcon(EmptyIcon.ICON_16)
                    myName = ""
                }
            }
        }

        fun parentStepIndex(): Int? {
            val stepNode = if (stepData != null) this else parentDescriptor as? StepTreeNode
            return stepNode?.stepData?.index
        }

        private fun messageLocation(message: ParsedStepMessage): String? {
            val raw = message.file?.name ?: message.fileName ?: return null
            val fileName = raw.substringAfterLast('/').substringAfterLast('\\')
            return if (fileName.isBlank()) null else fileName
        }
    }

    private inner class StepTreeStructure : AbstractTreeStructure() {

        override fun getRootElement(): Any = rootNode

        override fun getChildElements(element: Any): Array<Any> {
            val node = element as StepTreeNode
            return node.children.toTypedArray()
        }

        override fun getParentElement(element: Any): Any? {
            val node = element as StepTreeNode
            return node.parentDescriptor
        }

        override fun createDescriptor(element: Any, parentDescriptor: NodeDescriptor<*>?): NodeDescriptor<*> =
            element as NodeDescriptor<*>

        override fun hasSomethingToCommit(): Boolean = false

        override fun commit() {
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
