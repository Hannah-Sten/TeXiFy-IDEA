package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexRequiredParamContent
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.parentOfType
import nl.hannahsten.texifyidea.util.requiredParameters
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

        val fullCommand = command.name ?: return

        // Fetch the corresponding LatexRegularCommand object.
        val commandHuh = LatexCommand.lookup(fullCommand.substring(1)) ?: return

        val arguments = commandHuh.firstOrNull()?.getArgumentsOf(RequiredFileArgument::class.java)
        if (arguments?.isEmpty() == true) {
            return
        }

        // Actual required parameters from the PSI tree
        val requiredParams = command.requiredParameters()
        if (requiredParams.isEmpty()) {
            return
        }

        val referencesList = command.references.filterIsInstance<InputFileReference>()
        if (referencesList.isEmpty()) return

        val files = referencesList.mapNotNull { it.resolve() }
        val gutterFile = files.firstOrNull()?.virtualFile
        val extension = gutterFile?.let {
            if (it.name.endsWith("synctex.gz")) "synctex.gz" else it.extension
        }
        // Gutter requires a smaller icon per IJ SDK docs.
        val icon = TexifyIcons.getIconFromExtension(extension, smaller = true)

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
        return TexifyIcons.LATEX_FILE_SMALLER
    }
}
