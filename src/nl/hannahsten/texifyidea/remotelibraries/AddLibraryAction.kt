package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.treeStructure.Tree
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.util.TexifyDataKeys
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * Action to add a remote library to the libraries tool window.
 */
abstract class AddLibraryAction<Lib: RemoteBibLibrary, T: DialogWrapper> : AnAction() {

    /**
     * Add the elements from the library to the tree in the tool window.
     */
    override fun actionPerformed(e: AnActionEvent) {
        val dialogWrapper = getDialog(e.project ?: return)

        if(dialogWrapper.showAndGet()) {
            ApplicationManager.getApplication().invokeLater {
                runBlocking {
                    val (library, bibItems) = createLibrary(dialogWrapper, e.project!!)
                    val tree = e.getData(TexifyDataKeys.LIBRARY_TREE) as Tree
                    val model = tree.model as DefaultTreeModel
                    val root = model.root as DefaultMutableTreeNode
                    val libraryNode = DefaultMutableTreeNode(library.name)
                    bibItems.forEach { bib ->
                        val entryElement = BibtexStructureViewEntryElement(bib)
                        val entryNode = DefaultMutableTreeNode(entryElement)
                        libraryNode.add(entryNode)

                        // Each bib item has tags that show information, e.g., the author.
                        entryElement.children.forEach {
                            entryNode.add(DefaultMutableTreeNode(it))
                        }
                    }
                    root.add(libraryNode)
                    model.nodeStructureChanged(root)
                }
            }
        }
    }

    /**
     * Create the dialog that handles the logging in of the user.
     *
     * The elements of the library are added when the user clicks OK. When the user clicks Cancel, nothing happens.
     */
    abstract fun getDialog(project: Project): T

    /**
     * Get access to the user's online library, retrieve, store, and return the elements.
     */
    abstract suspend fun createLibrary(dialogWrapper: T, project: Project): Pair<Lib, List<BibtexEntry>>
}