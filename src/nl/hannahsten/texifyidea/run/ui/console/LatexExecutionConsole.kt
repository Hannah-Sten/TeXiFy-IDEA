package nl.hannahsten.texifyidea.run.ui.console

import com.intellij.build.Filterable
import com.intellij.build.events.MessageEvent
import com.intellij.build.events.impl.FileMessageEventImpl
import com.intellij.build.events.impl.MessageEventImpl
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.ide.IdeBundle
import com.intellij.ide.OccurenceNavigator
import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.runInEdt
import com.intellij.ui.AutoScrollToSourceHandler
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SideBorder
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.Tree
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.step.Step
import nl.hannahsten.texifyidea.util.files.findVirtualFileByAbsoluteOrRelativePath
import java.awt.BorderLayout
import java.awt.CardLayout
import java.util.function.Predicate
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * The tool window which shows the log messages and output log.
 * Partially re-implements NewErrorTreeViewPanel.
 *
 * todo copy more functionality from NewErrorTreeViewPanel
 *
 * @author Sten Wessel
 */
class LatexExecutionConsole(runConfig: LatexRunConfiguration) : ConsoleView, OccurenceNavigator, Filterable<Any>, ExecutionConsole {

    companion object {

        private const val SPLITTER_PROPORTION_PROPERTY = "TeXiFy.ExecutionConsole.Splitter.Proportion"
    }

    private data class StepUI(val step: Step, val node: LatexExecutionNode, val console: ConsoleView)

    private val project = runConfig.project

    private val container: JComponent
    private val tree: Tree
    private val treeModel: StructureTreeModel<AbstractTreeStructure>
    private val rootNode: LatexExecutionNode
    private val consolePanel: JPanel

    private val steps = mutableMapOf<String, StepUI>()

    init {
        rootNode = LatexExecutionNode(project)
        rootNode.title = runConfig.name

        treeModel = StructureTreeModel(TreeStructure(), this)
        tree = LatexExecutionTree(AsyncTreeModel(treeModel, this)).apply {
            initialize()
        }

        consolePanel = JPanel(CardLayout())

        container = JPanel(BorderLayout()).apply {
            val splitter = OnePixelSplitter(SPLITTER_PROPORTION_PROPERTY, 0.5f)
            with(splitter) {
                firstComponent = JPanel(CardLayout()).apply {
                    add(ScrollPaneFactory.createScrollPane(tree, SideBorder.NONE))
                }
                secondComponent = consolePanel
            }
            add(splitter, BorderLayout.CENTER)
        }

        val autoScrollToSourceHandler = object : AutoScrollToSourceHandler() {
            override fun isAutoScrollMode(): Boolean {
                return true // todo auto scroll to source
            }

            override fun setAutoScrollMode(state: Boolean) {
            }
        }
        autoScrollToSourceHandler.install(tree)

        // When selecting a step in the tree, the matching console output should show
        tree.selectionModel.addTreeSelectionListener(
            LatexExecutionTreeSelectionListener(tree) { node ->
                node.stepId?.let {
                    // Select the card which should show on top, based on its step id
                    (consolePanel.layout as CardLayout).show(consolePanel, it)
                }
            }
        )
    }

    fun start() {
        with(rootNode) {
            state = LatexExecutionNode.State.RUNNING
            description = "Running Compile Steps..."
            scheduleUpdate(this)
        }
    }

    fun finish(failed: Boolean = false) {
        with(rootNode) {
            state = if (failed) LatexExecutionNode.State.FAILED else LatexExecutionNode.State.SUCCEEDED
            description = if (failed) "Failed" else "Successful"
            scheduleUpdate(this)
        }
    }

    // todo relocate
    /**
     * Add log message to tree.
     */
    fun onEvent(event: MessageEventImpl) {
        val id = (event.parentId as? String) ?: return
        val (step, node, console) = steps[id] ?: return
        // todo should we really reuse the 'step' node for messages?
        // todo shouldn't this be parent=node?
        LatexExecutionNode(project, node.stepId, parent = rootNode).apply {
            description = event.message
            state = when (event.kind) {
                MessageEvent.Kind.WARNING -> LatexExecutionNode.State.WARNING
                MessageEvent.Kind.ERROR -> LatexExecutionNode.State.FAILED
                else -> LatexExecutionNode.State.UNKNOWN
            }
            if (event is FileMessageEventImpl && project != null) {
                file = findVirtualFileByAbsoluteOrRelativePath(event.filePosition.file.path, project!!)
                line = event.filePosition.startLine
            }
            node.children.add(this)
        }
        scheduleUpdate(node, true)
    }

    fun startStep(id: String, step: Step, handler: ProcessHandler) {
        val node = LatexExecutionNode(project, id, rootNode).apply {
            description = step.provider.name
            state = LatexExecutionNode.State.RUNNING
            file = step.configuration.options.mainFile.resolve()
            rootNode.children.add(this)
        }

        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        if (handler is OSProcessHandler) {
            console.print(handler.commandLine, ConsoleViewContentType.SYSTEM_OUTPUT)
        }
        console.print("\n\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        console.attachToProcess(handler)

        steps[id] = StepUI(step, node, console)

        scheduleUpdate(node, true)
        runInEdt {
            consolePanel.add(id, console.component)
            // This line probably doesn't do match, as by default the last added card is shown at the front of the card layout, but it shows how to select a card.
            (consolePanel.layout as CardLayout).show(consolePanel, id)
        }
    }

    fun finishStep(id: String, exitCode: Int) {
        val (_, node, console) = steps[id] ?: return
        node.state = when {
            exitCode != 0 -> {
                LatexExecutionNode.State.FAILED
            }
            node.children.any { it.state == LatexExecutionNode.State.FAILED } -> {
                LatexExecutionNode.State.FAILED
            }
            node.children.any { it.state == LatexExecutionNode.State.WARNING } -> {
                LatexExecutionNode.State.WARNING
            }
            else -> {
                LatexExecutionNode.State.SUCCEEDED
            }
        }
        console.print(IdeBundle.message("run.anything.console.process.finished", exitCode), ConsoleViewContentType.SYSTEM_OUTPUT)
        console.print("\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        scheduleUpdate(node)
    }

    private fun scheduleUpdate(node: LatexExecutionNode, parentStructureChanged: Boolean = false) {
        val changedNode = if (!parentStructureChanged || node.parent == null) node else node.parent
        treeModel.invalidate(changedNode, parentStructureChanged)
    }

    override fun getComponent() = container

    override fun getPreferredFocusableComponent() = component

    override fun dispose() {
    }

    override fun print(text: String, contentType: ConsoleViewContentType) {
//        TODO("Not yet implemented")
    }

    override fun clear() {
        treeModel.invoker.invoke {
            rootNode.children.clear()
            steps.clear()
        }
        scheduleUpdate(rootNode, true)
    }

    override fun scrollTo(offset: Int) {
        TODO("Not yet implemented")
    }

    override fun attachToProcess(processHandler: ProcessHandler) {
    }

    override fun setOutputPaused(value: Boolean) {
    }

    override fun isOutputPaused() = false

    override fun hasDeferredOutput() = false

    override fun performWhenNoDeferredOutput(runnable: Runnable) {
    }

    override fun setHelpId(helpId: String) {
    }

    override fun addMessageFilter(filter: Filter) {
    }

    override fun printHyperlink(hyperlinkText: String, info: HyperlinkInfo?) {
    }

    override fun getContentSize() = 0

    override fun canPause() = false

    override fun createConsoleActions() = AnAction.EMPTY_ARRAY

    override fun allowHeavyFilters() {
    }

    override fun hasNextOccurence(): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasPreviousOccurence(): Boolean {
        TODO("Not yet implemented")
    }

    override fun goNextOccurence(): OccurenceNavigator.OccurenceInfo {
        TODO("Not yet implemented")
    }

    override fun goPreviousOccurence(): OccurenceNavigator.OccurenceInfo {
        TODO("Not yet implemented")
    }

    override fun getNextOccurenceActionName(): String {
        TODO("Not yet implemented")
    }

    override fun getPreviousOccurenceActionName(): String {
        TODO("Not yet implemented")
    }

    override fun isFilteringEnabled() = true

    override fun getFilter(): Predicate<Any> {
        TODO("Not yet implemented")
    }

    override fun addFilter(filter: Predicate<in Any>) {
        TODO("Not yet implemented")
    }

    override fun removeFilter(filter: Predicate<in Any>) {
        TODO("Not yet implemented")
    }

    override fun contains(filter: Predicate<in Any>): Boolean {
        TODO("Not yet implemented")
    }

    private inner class TreeStructure : AbstractTreeStructure() {

        override fun getRootElement() = rootNode

        override fun getChildElements(element: Any) = (element as LatexExecutionNode).children.toTypedArray()

        override fun getParentElement(element: Any) = (element as LatexExecutionNode).parent

        override fun createDescriptor(element: Any, parentDescriptor: NodeDescriptor<*>?) = element as NodeDescriptor<*>

        override fun hasSomethingToCommit() = false

        override fun commit() {
        }
    }
}