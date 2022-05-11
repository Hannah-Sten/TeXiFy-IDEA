package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.codeStyle.extractor.ui.ExtractedSettingsDialog.CellRenderer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.treeStructure.Tree
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.util.allFiles
import nl.hannahsten.texifyidea.util.hasLatexModule
import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

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


    class RemoteLibrariesToolWindow(val project: Project)  {
        val library = RemoteLibraryManager.getInstance().libraries.toMap().entries.first()

        val treeNode = DefaultMutableTreeNode(library.key).apply {
            library.value.forEach {
                add(DefaultMutableTreeNode(it))
            }
        }

        val tree = Tree(treeNode).apply {
            setCellRenderer { tree, value, selected, expanded, leaf, row, hasFocus ->
                panel { row {
                    when(val userObject = (value as DefaultMutableTreeNode).userObject) {
                        is BibtexEntry -> {
                            val presentation = BibtexStructureViewEntryElement(userObject).presentation
                            icon(presentation.getIcon(true)!!)
                            label(presentation.presentableText!!)
                            label(presentation.locationString!!)
                        }
                        else -> label(value.toString())
                    }
                }}
            }
        }
        val content = JBScrollPane(tree)
    }

}