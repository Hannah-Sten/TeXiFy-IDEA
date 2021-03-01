package nl.hannahsten.texifyidea.run

import com.intellij.build.Filterable
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.ide.IdeBundle
import com.intellij.ide.OccurenceNavigator
import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.runInEdt
import com.intellij.ui.*
import com.intellij.ui.render.RenderingHelper
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.EditSourceOnEnterKeyHandler
import com.intellij.util.ui.tree.TreeUtil
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.step.LatexCompileStep
import java.awt.BorderLayout
import java.awt.CardLayout
import java.util.function.Predicate
import javax.swing.JComponent
import javax.swing.JPanel

class LatexExecutionConsole(runConfig: LatexRunConfiguration) : ConsoleView, OccurenceNavigator, Filterable<Any> {

    companion object {
        private const val SPLITTER_PROPORTION_PROPERTY = "TeXiFy.ExecutionConsole.Splitter.Proportion"

        private fun initTree(model: AsyncTreeModel) = Tree(model).apply {
            isLargeModel = true
            ComponentUtil.putClientProperty(this, AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
            isRootVisible = true
            EditSourceOnDoubleClickHandler.install(this)
            EditSourceOnEnterKeyHandler.install(this)
            TreeSpeedSearch(this).comparator = SpeedSearchComparator(false)
            TreeUtil.installActions(this)
            putClientProperty(RenderingHelper.SHRINK_LONG_RENDERER, true)
        }
    }

    private data class StepUI(val step: LatexCompileStep, val node: LatexExecutionNode, val console: ConsoleView)

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
        tree = initTree(AsyncTreeModel(treeModel, this))

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

    fun startStep(id: String, step: LatexCompileStep, handler: OSProcessHandler) {
        val node = LatexExecutionNode(project, rootNode).apply {
            description = step.provider.name
            state = LatexExecutionNode.State.RUNNING
            rootNode.children.add(this)
        }

        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        console.print(handler.commandLine, ConsoleViewContentType.SYSTEM_OUTPUT)
        console.print("\n\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        console.attachToProcess(handler)

        steps[id] = StepUI(step, node, console)

        scheduleUpdate(node, true)
        runInEdt {
            consolePanel.add(console.component, id)
            (consolePanel.layout as CardLayout).show(consolePanel, id)
        }
    }

    fun finishStep(id: String, exitCode: Int) {
        val (_, node, console) = steps[id] ?: return
        node.state = if (exitCode != 0) LatexExecutionNode.State.FAILED else LatexExecutionNode.State.SUCCEEDED
        console.print(IdeBundle.message("run.anything.console.process.finished", exitCode), ConsoleViewContentType.SYSTEM_OUTPUT)
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