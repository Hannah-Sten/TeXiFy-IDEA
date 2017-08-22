package nl.rubensten.texifyidea.project

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.packageDependencies.ui.PackageDependenciesNode
import com.intellij.ui.ColoredTreeCellRenderer
import nl.rubensten.texifyidea.TexifyIcons
import java.util.*
import javax.swing.Icon

/**
 * @author Ruben Schellekens
 */
class TeXiFyProjectViewNodeDecorator : ProjectViewNodeDecorator {

    companion object {

        private val FILE_ICONS = HashMap<String, Icon>()

        init {
            FILE_ICONS.put("pdf", TexifyIcons.PDF_FILE)
            FILE_ICONS.put("dvi", TexifyIcons.DVI_FILE)
        }
    }

    private fun setIcon(projectViewNode: ProjectViewNode<*>, presentationData: PresentationData) {
        val file = projectViewNode.virtualFile ?: return
        if (file.isDirectory) {
            return
        }

        val extension = file.extension ?: return
        val icon = FILE_ICONS[extension.toLowerCase()] ?: return

        presentationData.setIcon(icon)
    }

    override fun decorate(projectViewNode: ProjectViewNode<*>, presentationData: PresentationData) {
        setIcon(projectViewNode, presentationData)
    }

    override fun decorate(packageDependenciesNode: PackageDependenciesNode, coloredTreeCellRenderer: ColoredTreeCellRenderer) {
        // Do nothing.
    }
}
