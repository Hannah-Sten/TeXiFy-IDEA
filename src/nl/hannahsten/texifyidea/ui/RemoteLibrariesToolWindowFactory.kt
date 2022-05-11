package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.util.allFiles
import nl.hannahsten.texifyidea.util.hasLatexModule
import javax.swing.tree.DefaultMutableTreeNode

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
            library.value.forEach { add(DefaultMutableTreeNode(it.id!!.text)) }
        }
        val tree = Tree(treeNode).apply {

        }
        val content = JBScrollPane(tree)
    }

}