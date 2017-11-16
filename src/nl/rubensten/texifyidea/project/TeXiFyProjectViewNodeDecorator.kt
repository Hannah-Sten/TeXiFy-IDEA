package nl.rubensten.texifyidea.project

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.packageDependencies.ui.PackageDependenciesNode
import com.intellij.ui.ColoredTreeCellRenderer
import nl.rubensten.texifyidea.util.Magic

/**
 * @author Ruben Schellekens
 */
class TeXiFyProjectViewNodeDecorator : ProjectViewNodeDecorator {

    private fun setIcon(projectViewNode: ProjectViewNode<*>, presentationData: PresentationData) {
        val file = projectViewNode.virtualFile ?: return
        if (file.isDirectory) {
            return
        }

        var extension = file.extension ?: return
        if (extension == "gz") {
            extension = "synctex.gz"

            if (!file.name.toLowerCase().endsWith("synctex.gz")) {
                return
            }
        }

        val icon = Magic.Icon.fileIcons[extension.toLowerCase()] ?: return

        presentationData.setIcon(icon)
    }

    override fun decorate(projectViewNode: ProjectViewNode<*>, presentationData: PresentationData) {
        setIcon(projectViewNode, presentationData)
    }

    override fun decorate(packageDependenciesNode: PackageDependenciesNode, coloredTreeCellRenderer: ColoredTreeCellRenderer) {
        // Do nothing.
    }
}
