package nl.hannahsten.texifyidea.run.ui.console

import com.intellij.ui.treeStructure.Tree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode

class LatexExecutionTreeSelectionListener(val tree: Tree, val onSelected: (node: LatexExecutionNode) -> Unit) : TreeSelectionListener {
    override fun valueChanged(e: TreeSelectionEvent?) {
        val latexNode = (tree.lastSelectedPathComponent as? DefaultMutableTreeNode)?.userObject as? LatexExecutionNode ?: return
        onSelected(latexNode)
    }
}