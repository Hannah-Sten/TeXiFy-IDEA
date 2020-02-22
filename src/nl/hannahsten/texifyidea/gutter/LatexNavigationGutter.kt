package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer
import com.intellij.openapi.roots.ProjectRootManager
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

        // todo reuse code, same as filenotfoundinspection
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

        val argument = getRequiredFileArgument(ignoreFileArgument, commandName, arguments)
        val roots = getContentRoots(element) ?: return
        val graphicsPaths = getGraphicsPaths(element, fullCommand)
        val files: List<PsiFile> = getFiles(fileNames, element, roots, graphicsPaths, argument)

        val builder = buildGutterIcon(fileNames, files, ignoreFileArgument, argument)
        result.add(builder.createLineMarkerInfo(element))
    }

    private fun getRequiredFileArgument(ignoreFileArgument: Boolean, commandName: String, arguments: List<RequiredFileArgument>): RequiredFileArgument {

        return if (ignoreFileArgument) {
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
    }

    private fun getContentRoots(element: PsiElement): ArrayList<VirtualFile>? {
        // Look up target file.
        val containingFile = element.containingFile ?: return null

        val roots = ArrayList<VirtualFile>()
        val rootFile = containingFile.findRootFile()
        if (rootFile.containingDirectory == null) {
            return null
        }
        roots.add(rootFile.containingDirectory.virtualFile)
        val rootManager = ProjectRootManager.getInstance(element.project)
        Collections.addAll(roots, *rootManager.contentSourceRoots)

        return roots
    }

    private fun getGraphicsPaths(element: PsiElement, fullCommand: String): ArrayList<String> {
        // Get all comands of project.
        val allCommands = element.containingFile.commandsInFileSet()

        val graphicsPaths: ArrayList<String> = ArrayList()

        // Check if a graphicspath is defined
        val pathCommands = allCommands.filter { it.name == "\\graphicspath" }
        // Is a graphicspath defined?
        if (pathCommands.isNotEmpty()) {
            // Check if current command is a includegraphics
            if (fullCommand == "\\includegraphics") {
                val args = pathCommands[0].parameterList.filter { it.requiredParam != null }
                val subArgs = args[0].childrenOfType(LatexNormalText::class)
                subArgs.forEach { graphicsPaths.add(it.text) }
            }
        }

        return graphicsPaths
    }

    private fun getFiles(fileNames: List<String>, element: PsiElement, roots: ArrayList<VirtualFile>, graphPaths: ArrayList<String>, argument: RequiredFileArgument): List<PsiFile> {
        val psiManager = PsiManager.getInstance(element.project)

        return fileNames
                .map { fileName ->
                    for (root in roots) {
                        val file = java.io.File(fileName)
                        if (file.isAbsolute) {
                            com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(fileName)?.apply {
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
    }

    private fun buildGutterIcon(fileNames: List<String>, files: List<PsiFile>, ignoreFileArgument: Boolean, argument: RequiredFileArgument): NavigationGutterIconBuilder<PsiElement> {
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

        return NavigationGutterIconBuilder
                .create(icon)
                .setTargets(files)
                .setPopupTitle("Navigate to Referenced File")
                .setTooltipText("Go to referenced file")
                .setCellRenderer(GotoFileCellRenderer(0))
    }

    override fun getName(): String? {
        return "Navigate to referenced file"
    }

    override fun getIcon(): Icon? {
        return TexifyIcons.LATEX_FILE
    }
}
