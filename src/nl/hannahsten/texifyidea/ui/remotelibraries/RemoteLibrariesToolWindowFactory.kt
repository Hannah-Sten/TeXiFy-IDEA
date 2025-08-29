package nl.hannahsten.texifyidea.ui.remotelibraries

import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewTagElement
import nl.hannahsten.texifyidea.util.TexifyDataKeys
import nl.hannahsten.texifyidea.util.isLatexProject
import javax.swing.tree.DefaultMutableTreeNode

/**
 * The remote libraries tool window shows an overview of all remote libraries a user has connected with.
 *
 * @author Abby Berkers
 */
class RemoteLibrariesToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val librariesToolWindow = RemoteLibrariesToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(librariesToolWindow, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override suspend fun isApplicableAsync(project: Project) = readAction { project.isLatexProject() }

    /**
     * The tool window panel that contains the toolbar and the actual window (which is [RemoteLibraryToolWindow]).
     */
    class RemoteLibrariesToolWindowPanel(val project: Project) : SimpleToolWindowPanel(true, false), DataProvider {

        private val toolWindow = RemoteLibraryToolWindow(project)

        init {
            toolbar = toolWindow.toolbar.apply {
                targetComponent = this@RemoteLibrariesToolWindowPanel
            }.component

            setContent(toolWindow.content)
        }

        override fun getData(dataId: String): Any? = toolWindow.getData(dataId)
    }

    /**
     * The UI elements of the tool window contents. Most actual UI elements are taken from the structure view, with the
     * aim of this tree looking the same as the one in the structure view.
     */
    class RemoteLibraryToolWindow(val project: Project) : DataProvider {

        private val actionManager: ActionManager = ActionManager.getInstance()

        val toolbar = actionManager.createActionToolbar(
            ActionPlaces.TOOLWINDOW_TOOLBAR_BAR,
            actionManager.getAction("texify.remotelibraries") as ActionGroup,
            true
        )

        val libraries = RemoteLibraryManager.getInstance().getLibraries().toMap().entries

        private val rootNode = DefaultMutableTreeNode().apply {
            // Add all the bib items for each library.
            libraries.forEach { library ->
                val libraryNode = LibraryMutableTreeNode(library.key, library.value.displayName).apply {
                    library.value.entries.forEach { entry ->
                        val entryElement = BibtexStructureViewEntryElement(entry)
                        val entryNode = DefaultMutableTreeNode(entryElement)
                        add(entryNode)

                        // Each bib item has tags that show information, e.g., the author.
                        entryElement.children.forEach {
                            entryNode.add(DefaultMutableTreeNode(it))
                        }
                    }
                }

                add(libraryNode)
            }
        }

        val tree = Tree(rootNode).apply {
            // The root is an invisible dummy so multiple libraries can be shown in the tree alongside one another
            // without a redundant (and annoying) root element.
            isRootVisible = false

            // Nodes in the tree of the structure view are rendered by NodeRenderer, which internally uses PresentationData
            // (a subclass of ItemPresentation) to render a cell. We use the same renderer and presentation data here
            // for consistency.
            cellRenderer = object : NodeRenderer() {
                // We cannot depend on the StructureView to resolve the presentation for us, so we have to manually point
                // the renderer to our custom PresentationData (which we can reuse from the structure view's elements).
                override fun getPresentation(node: Any?): ItemPresentation? =
                    when (node) {
                        is BibtexStructureViewEntryElement -> node.presentation
                        is BibtexStructureViewTagElement -> node.presentation
                        else -> super.getPresentation(node)
                    }
            }
        }

        val content = JBScrollPane(tree)

        override fun getData(dataId: String): Any? {
            return when {
                TexifyDataKeys.LIBRARY_TREE.`is`(dataId) -> tree
                TexifyDataKeys.LIBRARY_NAME.`is`(dataId) -> (tree.selectionPath?.getPathComponent(1) as? LibraryMutableTreeNode)?.toString()
                TexifyDataKeys.LIBRARY_IDENTIFIER.`is`(dataId) -> (tree.selectionPath?.getPathComponent(1) as? LibraryMutableTreeNode)?.identifier
                else -> null
            }
        }
    }
}

class LibraryMutableTreeNode(val identifier: String, val displayName: String) : DefaultMutableTreeNode(displayName)