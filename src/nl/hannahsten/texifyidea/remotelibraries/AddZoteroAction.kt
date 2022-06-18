package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.treeStructure.Tree
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.util.TexifyDataKeys
import javax.swing.JComponent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class AddZoteroAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val dialogWrapper = AddZoteroDialogWrapper(e.project ?: return)

        if(dialogWrapper.showAndGet()) {
            ApplicationManager.getApplication().invokeLater {
                runBlocking {
                    val (library, bibItems) = createLibrary(dialogWrapper.userID, dialogWrapper.userApiKey, e.project!!)
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

    private suspend fun createLibrary(userID: String, apiKey: String, project: Project): Pair<ZoteroLibrary, List<BibtexEntry>> {
        val library = ZoteroLibrary(userID, apiKey)
        val credentials = Credentials(userID, apiKey)
        PasswordSafe.instance.set(ZoteroLibrary.credentialAttributes, credentials)
        val bibItems = library.getCollection(project)
        RemoteLibraryManager.getInstance().updateLibrary(library, bibItems)
        return library to bibItems
    }

    class AddZoteroDialogWrapper(val project: Project) : DialogWrapper(true) {
        var userID: String = ""

        var userApiKey: String = ""

        init {
            init()
        }

        override fun createCenterPanel(): JComponent {
            return panel {
                row("User ID:") {
                    textField().bindText({ userID }, { userID = it })
                    contextHelp("You can find your user ID in Zotero Settings > Feeds/API.")
                }
                row("User API key:") {
                    textField().bindText({ userApiKey }, { userApiKey = it })
                    contextHelp("Create a new API key in Zotero Settings > Feeds/API > Create new private key")
                }
            }
        }
    }
}