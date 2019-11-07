package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findRootFile
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
        // Only make markers when dealing with commands.
        if (element !is LatexCommands) {
            return
        }

        val fullCommand = element.commandToken.text ?: return

        // True when it doesnt have a required _file_ argument, but must be handled.
        val ignoreFileArgument = IGNORE_FILE_ARGUMENTS.contains(fullCommand)

        // Fetch the corresponding LatexRegularCommand object.
        val commandName = fullCommand.substring(1)
        val commandHuh = LatexRegularCommand[commandName]
        if (commandHuh == null && !ignoreFileArgument) {
            return
        }

        val arguments = commandHuh!!.getArgumentsOf(RequiredFileArgument::class.java)
        if (arguments.isEmpty() && !ignoreFileArgument) {
            return
        }

        // Get the required file arguments.
        val argument = if (ignoreFileArgument) {
            RequiredFileArgument("", "sty", "cls")
        } else {
            arguments[0]
        }

        val requiredParams = element.requiredParameters()
        if (requiredParams.isEmpty()) {
            return
        }

        // Make filename. Substring is to remove { and }.
        var fileName = requiredParams[0].group.text
        fileName = fileName.substring(1, fileName.length - 1)

        // Look up target file.
        val containingFile = element.getContainingFile() ?: return

        val roots = ArrayList<VirtualFile>()
        val rootFile = containingFile.findRootFile()
        if (rootFile.containingDirectory == null) {
            return
        }
        roots.add(rootFile.containingDirectory.virtualFile)
        val rootManager = ProjectRootManager.getInstance(element.getProject())
        Collections.addAll(roots, *rootManager.contentSourceRoots)

        var file: VirtualFile? = null
        for (root in roots) {
            val foundFile = root.findFile(fileName, argument.supportedExtensions)
            if (foundFile != null) {
                file = foundFile
                break
            }
        }

        if (file == null) {
            return
        }

        // Build gutter icon.
        val builder = NavigationGutterIconBuilder
                .create(TexifyIcons.getIconFromExtension(file.extension))
                .setTarget(PsiManager.getInstance(element.getProject()).findFile(file))
                .setTooltipText("Go to referenced file '" + file.name + "'")

        result.add(builder.createLineMarkerInfo(element))
    }

    override fun getName(): String? {
        return "Navigate to referenced file"
    }

    override fun getIcon(): Icon? {
        return TexifyIcons.LATEX_FILE
    }
}
