package nl.hannahsten.texifyidea.run.ui.console

import com.intellij.ide.errorTreeView.ErrorTreeNodeDescriptor
import com.intellij.ide.errorTreeView.NavigatableErrorTreeElement
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.ComponentUtil
import com.intellij.ui.SpeedSearchComparator
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.render.RenderingHelper
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.EditSourceOnEnterKeyHandler
import com.intellij.util.ui.tree.TreeUtil
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

/**
 * The tree used on the left of the [LatexExecutionConsole].
 */
class LatexExecutionTree(model: AsyncTreeModel) : Tree(model), DataProvider {
    // getData needs to be implemented on a component, we choose the Tree in this case (see DataProvider)
    override fun getData(dataId: String): Any? {
        // Used by the EditSourceOnDoubleClickHandler
        if (CommonDataKeys.NAVIGATABLE.`is`(dataId)) {
            return getSelectedNode()?.navigatable
        }
        return null
    }

    //region Copy from NewErrorTreeViewPanel

    private fun getSelectedNode(): LatexExecutionNode? {
        val nodes = getSelectedNodes()
        return if (nodes.size == 1) nodes[0] else null
    }

    private fun getSelectedNodes(): List<LatexExecutionNode> {
        val paths: Array<TreePath> = this.selectionPaths ?: return emptyList()
        val result: MutableList<LatexExecutionNode> = ArrayList()
        for (path in paths) {
            val lastPathNode = path.lastPathComponent as DefaultMutableTreeNode
            val userObject = lastPathNode.userObject
            if (userObject is LatexExecutionNode && userObject.file != null && userObject.project != null) {
                result.add(userObject)
            }
        }
        return result
    }
    //endregion

    fun initialize() {
        isLargeModel = true
        ComponentUtil.putClientProperty(this, AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
        isRootVisible = true
        EditSourceOnDoubleClickHandler.install(this)
        EditSourceOnEnterKeyHandler.install(this)
        // Does not seem to do anything
        TreeUtil.setNavigatableProvider(this) { path ->
            val lastPathNode = path.lastPathComponent as DefaultMutableTreeNode
            ((lastPathNode.userObject as? ErrorTreeNodeDescriptor)?.element as? NavigatableErrorTreeElement)?.navigatable
        }
        TreeSpeedSearch(this).comparator = SpeedSearchComparator(false)
        TreeUtil.installActions(this)
        putClientProperty(RenderingHelper.SHRINK_LONG_RENDERER, true)
    }
}