package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.ui.treeStructure.Tree
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.util.TexifyDataKeys
import java.util.Enumeration
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class SyncZoteroAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val credentials = PasswordSafe.instance.get(ZoteroLibrary.credentialAttributes)
        credentials?.userName?.let {
            credentials.password?.let { apiKey ->
                val zotero = ZoteroLibrary(it, apiKey.toString())
                ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Syncing Zotero...") {
                    lateinit var bibItems: List<BibtexEntry>
                    lateinit var expandedPaths: Enumeration<TreePath>
                    val tree by lazy { e.getData(TexifyDataKeys.LIBRARY_TREE) as Tree }


                    override fun run(indicator: ProgressIndicator) {
                        runBlocking {
                            expandedPaths = tree.getExpandedDescendants(TreePath(tree.model.root))
                            bibItems = zotero.getCollection(project)
                            RemoteLibraryManager.getInstance().updateLibrary(zotero, bibItems)
                        }
                    }

                    override fun onSuccess() {
                        ProgressManager.getInstance().run(object : Backgroundable(project, "Updating tree...") {
                            override fun run(indicator: ProgressIndicator) {
                                runReadAction {
                                    val model = tree.model as DefaultTreeModel
                                    val root = model.root as DefaultMutableTreeNode
                                    val libraryNode: DefaultMutableTreeNode = root.children()
                                        .asSequence()
                                        .firstOrNull { (it as DefaultMutableTreeNode).userObject == zotero.name } as? DefaultMutableTreeNode
                                        ?: DefaultMutableTreeNode(zotero.name)
                                    libraryNode.children().asSequence()
                                        .map { it as DefaultMutableTreeNode }
                                        .filter { (it.userObject as BibtexStructureViewEntryElement).entry.identifier !in bibItems.map { bib -> bib.identifier } }
                                        .forEach { libraryNode.remove(it) }

                                    val itemsInLib = libraryNode.children().asSequence()
                                        .map { ((it as DefaultMutableTreeNode).userObject as BibtexStructureViewEntryElement).entry.identifier}
                                        .toList()
                                    bibItems.forEach { bib ->
                                        if (bib.identifier !in itemsInLib) {
                                            val entryElement = BibtexStructureViewEntryElement(bib)
                                            val entryNode = DefaultMutableTreeNode(entryElement)
                                            libraryNode.add(entryNode)

                                            // Each bib item has tags that show information, e.g., the author.
                                            entryElement.children.forEach {
                                                entryNode.add(DefaultMutableTreeNode(it))
                                            }
                                        }
                                    }

                                    root.add(libraryNode)
                                    model.nodeStructureChanged(root)
                                    expandedPaths.asIterator().forEach { tree.expandPath(it) }
                                }
                            }
                        })
                    }
                })
            }
        }
    }
}