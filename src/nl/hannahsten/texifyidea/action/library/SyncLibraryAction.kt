package nl.hannahsten.texifyidea.action.library

import arrow.core.getOrElse
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.Tree
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibrary
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibraryFactory
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.ui.remotelibraries.LibraryMutableTreeNode
import nl.hannahsten.texifyidea.ui.remotelibraries.findLibraryNode
import nl.hannahsten.texifyidea.util.TexifyDataKeys
import nl.hannahsten.texifyidea.util.parser.getIdentifier
import java.util.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class SyncLibraryAction : AnAction() {

    /**
     * Synchronize the libraries that are selected in the library tree view. When there is no library selected - or the
     * user doesn't have the library tool window open - synchronize all the libraries.
     */
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val libraries = RemoteBibLibraryFactory
            .fromStorage(e.getData(TexifyDataKeys.LIBRARY_IDENTIFIER))
            ?.let { listOf(it) }
            // Fallback to synchronizing all the libraries.
            ?: RemoteLibraryManager.getInstance().getLibraries()
                .map {
                    RemoteBibLibraryFactory.fromStorage(it.key)
                }

        libraries.filterNotNull().forEach { syncLibrary(it, project, e) }
    }

    private fun syncLibrary(library: RemoteBibLibrary, project: Project, e: AnActionEvent) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Syncing ${library.displayName}...") {
            lateinit var bibItems: List<BibtexEntry>
            lateinit var expandedPaths: Enumeration<TreePath>
            val tree: Tree? by lazy { e.getData(TexifyDataKeys.LIBRARY_TREE) }

            override fun run(indicator: ProgressIndicator) {
                runBlocking {
                    bibItems = library.getCollection().getOrElse {
                        library.showNotification(e.project!!, it.libraryName, it.response)
                        // Apparently this is the way to cancel the task (and thus to avoid going into the onSuccess).
                        throw ProcessCanceledException()
                    }
                    RemoteLibraryManager.getInstance().updateLibrary(library, bibItems)
                    expandedPaths =
                        tree?.let { it.getExpandedDescendants(TreePath(it.model.root)) } ?: return@runBlocking

                }
            }

            override fun onSuccess() {
                // If the tree is null the user hasn't opened the library tool window yet, so there is no tree to update.
                // The tree will be drawn with the updated library elements once the user opens the tool window.
                tree?.let {
                    ProgressManager.getInstance().run(object : Backgroundable(project, "Updating tree...") {
                        override fun run(indicator: ProgressIndicator) {
                            runReadAction {
                                val model = it.model as DefaultTreeModel
                                val root = model.root as? DefaultMutableTreeNode ?: return@runReadAction
                                val libraryNode: LibraryMutableTreeNode = it.findLibraryNode(library.identifier)
                                    ?: LibraryMutableTreeNode(library.identifier, library.displayName)
                                libraryNode.children().asSequence()
                                    .map { it as DefaultMutableTreeNode }
                                    .filter { (it.userObject as BibtexStructureViewEntryElement).entry.getIdentifier() !in bibItems.map { bib -> bib.getIdentifier() } }
                                    .forEach { libraryNode.remove(it) }

                                val itemsInLib = libraryNode.children().asSequence()
                                    .map { ((it as DefaultMutableTreeNode).userObject as BibtexStructureViewEntryElement).entry.getIdentifier() }
                                    .toList()
                                bibItems.forEach { bib ->
                                    if (bib.getIdentifier() !in itemsInLib) {
                                        val entryElement = BibtexStructureViewEntryElement(bib)
                                        val entryNode = DefaultMutableTreeNode(entryElement)
                                        libraryNode.add(entryNode)

                                        // Each bib item has tags that show information, e.g., the author.
                                        entryElement.children.forEach {
                                            entryNode.add(DefaultMutableTreeNode(it))
                                        }
                                    }
                                }

                                if (!root.children().contains(libraryNode)) root.add(libraryNode)
                                model.nodeStructureChanged(root)
                                expandedPaths.asIterator().forEach(it::expandPath)
                            }
                        }
                    })
                }
            }
        })
    }
}

fun <T> Enumeration<T>.contains(element: T): Boolean {
    while (hasMoreElements()) {
        if (nextElement() == element) return true
    }
    return false
}