package nl.hannahsten.texifyidea.ui

import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.packageDependencies.ui.PackageDependenciesNode
import com.intellij.ui.ColoredTreeCellRenderer
import nl.hannahsten.texifyidea.util.magic.IconMagic
import java.util.*

/**
 * @author Hannah Schellekens
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

        // Allow Material design plugins to take over the icons
        // For file types registered in plugin.xml this happens automatically
        if (PluginManager.getLoadedPlugins().none { it.name.contains("Material") }) {
            val icon = IconMagic.fileIcons[extension.toLowerCase()] ?: return
            presentationData.setIcon(icon)
        }
    }

    override fun decorate(projectViewNode: ProjectViewNode<*>, presentationData: PresentationData) {
        setIcon(projectViewNode, presentationData)
    }

    override fun decorate(packageDependenciesNode: PackageDependenciesNode, coloredTreeCellRenderer: ColoredTreeCellRenderer) {
        // Do nothing.
    }
}
