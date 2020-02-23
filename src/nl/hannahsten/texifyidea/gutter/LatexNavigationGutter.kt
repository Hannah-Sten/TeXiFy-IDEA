package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.parentOfType
import nl.hannahsten.texifyidea.util.requiredParameters
import nl.hannahsten.texifyidea.util.splitContent
import java.io.File
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

        val fullCommand = command.commandToken.text ?: return

        // True when it doesn't have a required _file_ argument, but must be handled.
        val ignoreFileArgument = IGNORE_FILE_ARGUMENTS.contains(fullCommand)

        // Fetch the corresponding LatexRegularCommand object.
        val commandName = fullCommand.substring(1)
        val commandHuh = LatexRegularCommand[commandName]
        if (commandHuh == null && !ignoreFileArgument) {
            return
        }

        val arguments = commandHuh?.firstOrNull()?.getArgumentsOf(RequiredFileArgument::class.java)
        if (arguments?.isNullOrEmpty() == true && !ignoreFileArgument) {
            return
        }

        val requiredParams = command.requiredParameters()
        if (requiredParams.isEmpty()) {
            return
        }

        // Find filenames.
        val fileNames = splitContent(requiredParams[0], ",")

        // Get the required file arguments.
        val argument = if (ignoreFileArgument) {
            if (commandName == LatexRegularCommand.DOCUMENTCLASS.command) {
                RequiredFileArgument("", "cls")
            }
            else {
                RequiredFileArgument("", "sty")
            }
        }
        else {
            arguments[0]
        }

        // Look up target file.
        val containingFile = element.containingFile ?: return

        val roots = ArrayList<VirtualFile>()
        val rootFile = containingFile.findRootFile()
        if (rootFile.containingDirectory == null) {
            return
        }
        roots.add(rootFile.containingDirectory.virtualFile)
        val rootManager = ProjectRootManager.getInstance(element.project)
        Collections.addAll(roots, *rootManager.contentSourceRoots)

        val psiManager = PsiManager.getInstance(element.project)

        // Get all comands of project.
        val allCommands = element.containingFile.commandsInFileSet()

        val graphPaths: ArrayList<String> = ArrayList()

        // Check if a graphicspath is defined
        val pathCommands = allCommands.filter { it.name == "\\graphicspath" }
        // Is a graphicspath defined?
        if (pathCommands.isNotEmpty()) {
            // Check if current command is a includegraphics
            if (fullCommand == "\\includegraphics") {
                val args = pathCommands[0].parameterList.filter { it.requiredParam != null }
                val subArgs = args[0].childrenOfType(LatexNormalText::class)
                subArgs.forEach { graphPaths.add(it.text) }
            }
        }

        val files: List<PsiFile> = fileNames
                .map { fileName ->
                    for (root in roots) {
                        val file = File(fileName)
                        if (file.isAbsolute) {
                            LocalFileSystem.getInstance().findFileByPath(fileName)?.apply {
                                return@map psiManager.findFile(this)
                            }
                        }
                        else {
                            // Iterate through defined Graphicpaths
                            graphPaths.forEach {
                                root.findFile(it + fileName, argument.supportedExtensions)?.apply {
                                    return@map psiManager.findFile(this)
                                }
                                // Find also files defined by absolute path
                                root.fileSystem.findFileByPath(it + fileName)?.apply {
                                    return@map psiManager.findFile(this)
                                }
                            }
                            // Find files in root folder
                            root.findFile(fileName, argument.supportedExtensions)?.apply {
                                return@map psiManager.findFile(this)
                            }
                        }
                    }
                    null
                }
                .filterNotNull()
                .toList()

        // Build gutter icon.

        // Get the icon from the file extension when applicable and there exists an icon for this extension,
        // otherwise get the default icon for this argument.
        val extension = fileNames.firstOrNull()?.split(".")?.last()
        val defaultIcon = TexifyIcons.getIconFromExtension(argument.defaultExtension)
        val icon = if (ignoreFileArgument || TexifyIcons.getIconFromExtension(extension) == TexifyIcons.FILE) {
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
