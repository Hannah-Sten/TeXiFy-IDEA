package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.files.getFileExtension
import nl.hannahsten.texifyidea.util.getFileArgumentsReferences
import nl.hannahsten.texifyidea.util.parentOfType
import nl.hannahsten.texifyidea.util.requiredParameters
import java.util.*
import javax.swing.Icon

/**
 * @author Hannah Schellekens
 */
class LatexNavigationGutter : RelatedItemLineMarkerProvider() {

    companion object {
        private val IGNORE_FILE_ARGUMENTS = HashSet(listOf("\\RequirePackage", "\\usepackage", "\\documentclass", "\\LoadClass", "\\LoadClassWithOptions"))
    }

    override fun collectNavigationMarkers(element: PsiElement,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {

        // Gutters should only be used with leaf elements.
        // Filter for text nodes and then lookup their LatexCommands parent
        if (element.firstChild != null || element.parent !is LatexNormalText) return

        // Only make markers when dealing with commands.
        val command = element.parentOfType(LatexCommands::class) ?: return

        val fullCommand = command.name ?: return

        // True when it doesn't have a required _file_ argument, but must be handled.
        val ignoreFileArgument = IGNORE_FILE_ARGUMENTS.contains(fullCommand)

        // Fetch the corresponding LatexRegularCommand object.
        val commandName = fullCommand.substring(1)
        val commandHuh = LatexRegularCommand[commandName] ?: return

        val arguments = commandHuh.firstOrNull()?.getArgumentsOf(RequiredFileArgument::class.java)
        if (arguments?.isNullOrEmpty() == true) {
            return
        }

        // Actual required parameters from the PSI tree
        val requiredParams = command.requiredParameters()
        if (requiredParams.isEmpty()) {
            return
        }

        val referencesList = command.getFileArgumentsReferences()
        if (referencesList.isEmpty()) return
        val files = referencesList.mapNotNull { it.resolve() }

        val defaultIcon = TexifyIcons.getIconFromExtension(referencesList.first().defaultExtension)
        val extension = if (files.isNotEmpty()) { files.first().name.getFileExtension() } else ""
        val icon = if (ignoreFileArgument || (!files.isNullOrEmpty() && TexifyIcons.getIconFromExtension(extension) == TexifyIcons.FILE)) {
            defaultIcon
        }
        else {
            TexifyIcons.getIconFromExtension(extension)
        }

        val builder = NavigationGutterIconBuilder
                .create(icon)
                .setTargets(files)
                .setPopupTitle("Navigate to Referenced File")
                .setTooltipText("Go to referenced file")
                .setCellRenderer(GotoFileCellRenderer(0))

        result.add(builder.createLineMarkerInfo(element))
    }

    override fun getName(): String? {
        return "Navigate to referenced file"
    }

    override fun getIcon(): Icon? {
        return TexifyIcons.LATEX_FILE
    }
}
