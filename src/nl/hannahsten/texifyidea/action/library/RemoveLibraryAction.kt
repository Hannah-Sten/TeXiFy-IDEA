package nl.hannahsten.texifyidea.action.library

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.treeStructure.Tree
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.util.TexifyDataKeys
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class RemoveLibraryAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val library = e.getData(TexifyDataKeys.LIBRARY_NAME) ?: return
        RemoteLibraryManager.getInstance().removeLibraryByKey(library)

        val tree = e.getData(TexifyDataKeys.LIBRARY_TREE) as Tree
        val model = tree.model as DefaultTreeModel
        val root = model.root as DefaultMutableTreeNode
        root.removeAllChildren()
        model.nodeStructureChanged(root)
    }
}