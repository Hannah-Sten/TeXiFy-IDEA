package nl.rubensten.texifyidea.project

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.packageDependencies.ui.PackageDependenciesNode
import com.intellij.ui.ColoredTreeCellRenderer
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Ruben Schellekens
 */
class TeXiFyProjectViewNodeDecorator : ProjectViewNodeDecorator {

    companion object {

        private val FILE_ICONS = mapOf(
                "pdf" to TexifyIcons.PDF_FILE,
                "dvi" to TexifyIcons.DVI_FILE,
                "synctex.gz" to TexifyIcons.SYNCTEX_FILE,
                "bbl" to TexifyIcons.BBL_FILE,
                "aux" to TexifyIcons.AUX_FILE,
                "tmp" to TexifyIcons.TEMP_FILE,
                "dtx" to TexifyIcons.DOCUMENTED_LATEX_SOURCE,
                "bib" to TexifyIcons.BIBLIOGRAPHY_FILE
        )
    }

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
