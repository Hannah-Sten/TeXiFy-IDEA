package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer
import com.intellij.platform.ide.progress.ModalTaskOwner.project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.TexifyIcons.FILE
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexRequiredParamContent
import nl.hannahsten.texifyidea.util.Log
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
        if (element.firstChild != null || element.parentOfType(LatexRequiredParamContent::class) == null) return

        // Only make markers when dealing with commands.
        val command = element.parentOfType(LatexCommands::class) ?: return
        val text = element.text
        val (textList, filesList) = LatexProjectStructure.commandFileReferenceInfo(command) ?: return
        val idx = textList.indexOf(text)
        if(idx < 0) return
        val virtualFiles = filesList[idx]
        val manager = PsiManager.getInstance(element.project)
        val files = virtualFiles.mapNotNull { manager.findFile(it) }
        val extension = virtualFiles.firstOrNull()?.let {
            if (it.name.endsWith("synctex.gz")) "synctex.gz" else it.extension
        }
        // Gutter requires a smaller icon per IJ SDK docs.
        val icon = TexifyIcons.getIconFromExtension(extension, default = FILE) ?: return

        try {
            val builder = NavigationGutterIconBuilder
                .create(icon)
                .setTargets(files)
                .setPopupTitle("Navigate to Referenced File")
                .setTooltipText("Go to referenced file")
                .setCellRenderer { GotoFileCellRenderer(0) }

            result.add(builder.createLineMarkerInfo(element))
        }
        catch (e: NoSuchMethodError) {
            // I have no idea what could lead to the setCellRenderer method not existing, as it almost always exists, but in April 2022 there are suddenly 10 reports in which it doesn't.
            Log.warn(e.message ?: "NoSuchMethodError in LatexNavigationGutter")
        }
    }

    override fun getName(): String {
        return "Navigate to referenced file"
    }

    override fun getIcon(): Icon {
        return TexifyIcons.LATEX_FILE
    }
}
