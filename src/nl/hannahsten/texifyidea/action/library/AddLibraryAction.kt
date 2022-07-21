package nl.hannahsten.texifyidea.action.library

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.RemoteLibraryRequestTeXception
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibrary
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.ui.remotelibraries.AddLibDialogWrapper
import nl.hannahsten.texifyidea.ui.remotelibraries.LibraryMutableTreeNode
import nl.hannahsten.texifyidea.util.TexifyDataKeys
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * Action to add a remote library to the libraries tool window.
 *
 * To create an action to add a remote library, subclass this one and implement the abstract members. Then add that action
 * to plugin.xml.
 */
abstract class AddLibraryAction<Lib : RemoteBibLibrary, T : AddLibDialogWrapper> : AnAction() {

    /**
     * Add the elements from the library to the tree in the tool window.
     */
    override fun actionPerformed(e: AnActionEvent) {
        val dialogWrapper: T = getDialog(e.project ?: return)

        if (dialogWrapper.showAndGet()) {
            ProgressManager.getInstance()
                .run(object : Backgroundable(e.project, "Adding ${dialogWrapper.displayName} library") {
                    lateinit var library: Lib
                    lateinit var bibItems: List<BibtexEntry>

                    override fun run(indicator: ProgressIndicator) {
                        runBlocking {
                            try {
                                // Cannot be destructured directly.
                                val (libraryT, bibItemsT) = createLibrary(dialogWrapper, e.project!!) ?: return@runBlocking
                                library = libraryT
                                bibItems = bibItemsT
                            }
                            catch (exception: RemoteLibraryRequestTeXception) {
                                exception.showNotification(e.project!!)
                                // Apparently this is the way to cancel the task (and thus to avoid going into the onSuccess).
                                throw ProcessCanceledException(exception)
                            }
                        }
                    }

                    override fun onSuccess() {
                        // Only add the node if we successfully retrieve the bib items, and there are bib items to show.
                        if (bibItems.isEmpty()) return
                        val tree = e.getData(TexifyDataKeys.LIBRARY_TREE) ?: return
                        val model = tree.model as DefaultTreeModel
                        val root = model.root as DefaultMutableTreeNode

                        val libraryNode = LibraryMutableTreeNode(library.identifier, library.displayName)
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
                })
        }

        onFinish()
    }

    /**
     * Override this method to do something after the library has been added.
     */
    open fun onFinish() = Unit

    /**
     * Create the dialog that handles the logging in of the user.
     *
     * The elements of the library are added when the user clicks OK. When the user clicks Cancel, nothing happens.
     */
    abstract fun getDialog(project: Project): T

    /**
     * Get access to the user's online library, retrieve, store, and return the elements.
     */
    abstract suspend fun createLibrary(dialogWrapper: T, project: Project): Pair<Lib, List<BibtexEntry>>?
}