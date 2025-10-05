package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.codeInsight.navigation.impl.PsiTargetPresentationRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.TexifyIcons.FILE
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.parser.parentOfType
import javax.swing.Icon

/**
 * @author Hannah Schellekens
 */
class LatexNavigationGutter : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        // Gutters should only be used with leaf elements.
        // We assume gutter icons only have to be shown for elements in required parameters
        if(element.elementType != LatexTypes.COMMAND_TOKEN) return
//        if (element.firstChild != null) return

        // Only make markers when dealing with commands.
        val command = element.parentOfType(LatexCommands::class) ?: return
        val (_, filesList) = LatexProjectStructure.commandFileReferenceInfo(command) ?: return
        val virtualFiles = filesList.flatten().filter { it.isValid }
        val extension = virtualFiles.firstOrNull()?.let {
            if (it.name.endsWith("synctex.gz")) "synctex.gz" else it.extension
        }
        // Gutter requires a smaller icon per IJ SDK docs.
        val icon = TexifyIcons.getIconFromExtension(extension, default = FILE) ?: return
        val psiFiles = InputFileReference.findValidPSIFiles(virtualFiles, element.project)

        val builder = NavigationGutterIconBuilder
            .create(icon)
            .setTargets(psiFiles)
            .setPopupTitle("Navigate to Referenced File")
            .setTooltipText("Go to referenced file")
            .setTargetRenderer {
                // use new api
                PsiTargetPresentationRenderer()
            }
        result.add(builder.createLineMarkerInfo(element))
    }

    override fun getName(): String {
        return "Navigate to referenced file"
    }

    override fun getIcon(): Icon {
        return TexifyIcons.LATEX_FILE
    }
}
