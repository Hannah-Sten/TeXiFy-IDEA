package nl.hannahsten.texifyidea.ui.remotelibraries

import com.intellij.ui.treeStructure.Tree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

fun Tree.findLibraryNode(identifier: String): LibraryMutableTreeNode? {
    val model = model as DefaultTreeModel
    val root = model.root as? DefaultMutableTreeNode
    return root?.children()
        ?.asSequence()
        ?.firstOrNull { (it as LibraryMutableTreeNode).identifier == identifier } as? LibraryMutableTreeNode
}