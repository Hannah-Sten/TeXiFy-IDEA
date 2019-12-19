package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ex.WindowManagerEx
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.requiredParameters
import nl.hannahsten.texifyidea.util.splitContent
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

        // True when it doesn't have a required _file_ argument, but must be handled.
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

        val requiredParams = element.requiredParameters()
        if (requiredParams.isEmpty()) {
            return
        }

        // Find filenames.
        val fileNames = splitContent(requiredParams[0], ",")

        // Get the required file arguments.
        val argument = if (ignoreFileArgument) {
            if (commandName == LatexRegularCommand.DOCUMENTCLASS.command) {
                RequiredFileArgument("", "cls")
            } else {
                RequiredFileArgument("", "sty")
            }
        } else {
            arguments[0]
        }


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

        val psiManager = PsiManager.getInstance(element.project)

        val files: List<PsiFile> = fileNames
                .map { fileName ->
                    for (root in roots) {
                        val foundFile = root.findFile(fileName, argument.supportedExtensions)
                        if (foundFile != null) {
                            return@map psiManager.findFile(foundFile)
                        }
                    }
                    null
                }
                .filterNotNull()
                .toList()

        // Build gutter icon.
        val maxSize = WindowManagerEx.getInstanceEx().getFrame(element.getProject())?.size?.width
                ?: return

        // Get the icon from the file extension when applicable and there exists an icon for this extension,
        // otherwise get the default icon for this argument.
        val extensionIcon = TexifyIcons.getIconFromExtension(fileNames[0].split(".").last())
        val icon = if (ignoreFileArgument || extensionIcon == TexifyIcons.FILE) {
            TexifyIcons.getIconFromExtension(argument.defaultExtension)
        } else {
            extensionIcon
        }

        val builder = NavigationGutterIconBuilder
                .create(icon)
                .setTargets(files)
                .setPopupTitle("Navigate to Referenced File")
                .setTooltipText("Go to referenced file")
                .setCellRenderer(GotoFileCellRenderer(maxSize))

        result.add(builder.createLineMarkerInfo(element))
    }

    override fun getName(): String? {
        return "Navigate to referenced file"
    }

    override fun getIcon(): Icon? {
        return TexifyIcons.LATEX_FILE
    }
}
