package nl.hannahsten.texifyidea.action.library

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.treeStructure.Tree
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibraryFactory
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.ui.remotelibraries.findLibraryNode
import nl.hannahsten.texifyidea.util.TexifyDataKeys
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class RemoveLibraryAction : AnAction() {

    /**
     * Removing a library does the following:
     *
     * - Remove all credentials from the password safe.
     * - Remove the library from the [RemoteLibraryManager], this permanently erases the information stored on disk.
     * - Remove the library node (and its children) from the tree in the libraries tool window.
     */
    override fun actionPerformed(e: AnActionEvent) {
        val libraryKey = e.getData(TexifyDataKeys.LIBRARY_IDENTIFIER) ?: return
        RemoteBibLibraryFactory.fromStorage(libraryKey)?.destroyCredentials()
        RemoteLibraryManager.getInstance().removeLibraryByKey(libraryKey)

        val tree = e.getData(TexifyDataKeys.LIBRARY_TREE) as Tree
        val model = tree.model as DefaultTreeModel
        val root = model.root as DefaultMutableTreeNode
        root.remove(tree.findLibraryNode(libraryKey))
        model.nodeStructureChanged(root)
    }
}