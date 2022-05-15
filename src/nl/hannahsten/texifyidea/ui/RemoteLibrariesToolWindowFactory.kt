package nl.hannahsten.texifyidea.ui

import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewTagElement
import nl.hannahsten.texifyidea.util.allFiles
import nl.hannahsten.texifyidea.util.hasLatexModule
import javax.swing.tree.DefaultMutableTreeNode

/**
 * The remote libraries tool window shows an overview of all remote libraries a user has connected with.
 *
 * @author Abby Berkers
 */
class RemoteLibrariesToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val librariesToolWindow = RemoteLibrariesToolWindow(project)
        val content = ContentFactory.SERVICE.getInstance().createContent(librariesToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun isApplicable(project: Project) =
        if (ApplicationNamesInfo.getInstance().scriptName == "idea") {
            project.hasLatexModule()
        }
        // Non-idea has no concept of modules so we need to use some other criterion based on the project
        else {
            project.allFiles(LatexFileType).isNotEmpty()
        }


    /**
     * The UI elements of the tool window. Most actual UI elements are taken from the structure view, with the aim of
     * this tree looking the same as the one in the structure view.
     */
    class RemoteLibrariesToolWindow(val project: Project) {

        val libraries = RemoteLibraryManager.getInstance().libraries.toMap().entries

        val rootNode = DefaultMutableTreeNode().apply {
            // Add all the bib items for each library.
            libraries.forEach { library ->
                val libraryNode = DefaultMutableTreeNode(library.key).apply {
                    library.value.forEach { entry ->
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
                // the renderer to our custom PresentationData (which we can reuse).
                override fun getPresentation(node: Any?): ItemPresentation? =
                    when (node) {
                        is BibtexStructureViewEntryElement -> node.presentation
                        is BibtexStructureViewTagElement -> node.presentation
                        else -> super.getPresentation(node)
                    }
            }
        }

        val content = JBScrollPane(tree)
    }

}