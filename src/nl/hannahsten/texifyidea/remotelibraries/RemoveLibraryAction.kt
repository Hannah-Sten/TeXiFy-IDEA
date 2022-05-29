package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.treeStructure.Tree
import javax.swing.tree.DefaultMutableTreeNode

class RemoveLibraryAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val library = e.dataContext.getData("library") as String
        RemoteLibraryManager.getInstance().removeLibraryByKey(library)
    }
}