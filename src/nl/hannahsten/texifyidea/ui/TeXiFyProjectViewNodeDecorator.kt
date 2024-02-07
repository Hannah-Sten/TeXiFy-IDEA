package nl.hannahsten.texifyidea.ui

import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import nl.hannahsten.texifyidea.TexifyIcons
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

            if (!file.name.lowercase(Locale.getDefault()).endsWith("synctex.gz")) {
                return
            }
        }

        // Allow Material design plugins to take over the icons
        // For file types registered in plugin.xml this happens automatically
        if (PluginManager.getLoadedPlugins().none { it.name.contains("Material") }) {
            // Make sure to now override non-LaTeX extensions with the default icon
            val icon = TexifyIcons.getIconFromExtension(extension.lowercase(Locale.getDefault()), default = null) ?: return
            presentationData.setIcon(icon)
        }
    }

    override fun decorate(projectViewNode: ProjectViewNode<*>, presentationData: PresentationData) {
        setIcon(projectViewNode, presentationData)
    }
}
