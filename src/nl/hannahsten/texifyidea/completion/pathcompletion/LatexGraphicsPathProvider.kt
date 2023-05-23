package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.psi.childrenOfType
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.magic.cmd
import java.io.File

/**
 * Autocompletion roots based on graphicspaths.
 */
class LatexGraphicsPathProvider : LatexPathProviderBase() {

    override fun selectScanRoots(file: PsiFile): ArrayList<VirtualFile> {
        val paths = getProjectRoots()

        val rootDirectory = file.findRootFile().containingDirectory.virtualFile
        getGraphicsPathsInFileSet(file).forEach {
            paths.add(rootDirectory.findVirtualFileByAbsoluteOrRelativePath(it) ?: return@forEach)
        }

        return paths
    }

    /**
     * When using \includegraphics from graphicx package, a path prefix can be set with \graphicspath.
     * @return Graphicspaths defined in the fileset.
     */
    fun getGraphicsPathsInFileSet(file: PsiFile): List<String> {
        val graphicsPaths = mutableListOf<String>()
        val graphicsPathCommands = file.commandsInFileSet().filter { it.name == LatexGenericRegularCommand.GRAPHICSPATH.cmd }

        // Is a graphicspath defined?
        if (graphicsPathCommands.isNotEmpty()) {
            // Only last defined one counts
            graphicsPathCommands.last().getGraphicsPaths().forEach { graphicsPaths.add(it) }
        }

        return graphicsPaths
    }

    /**
     * This function is used in [InputFileReference#resolve], which is also used to create the file set, so we should
     * not use the file set here (to avoid an infinite loop). Instead, we start searching the file of the given command
     * for graphics paths. Then look at all commands that include the file of the given command and check the files of
     * those commands for graphics paths.
     */
    fun getGraphicsPathsWithoutFileSet(command: LatexCommands): List<String> {
        fun graphicsPathsInFile(file: PsiFile): List<String> = file.commandsInFile()
            .filter { it.name == "\\graphicspath" }
            .flatMap { it.getGraphicsPaths() }

        // First find all graphicspaths commands in the file of the given command
        val graphicsPaths = graphicsPathsInFile(command.containingFile).toMutableList()

        val allIncludeCommands = LatexIncludesIndex.getItems(command.project)
        // Commands which may include the current file (this is an overestimation, better would be to check for RequiredFileArguments)
        var includingCommands = allIncludeCommands.filter { includeCommand -> includeCommand.requiredParameters.any { it.contains(command.containingFile.name.removeFileExtension()) } }

        // Avoid endless loop (in case of a file inclusion loop)
        val maxDepth = allIncludeCommands.size
        var counter = 0

        // I think it's a kind of reversed BFS
        while (includingCommands.isNotEmpty() && counter < maxDepth) {
            val handledFiles = mutableListOf<PsiFile>()
            val newIncludingCommands = mutableListOf<LatexCommands>()

            for (includingCommand in includingCommands) {
                // Search the file of the command that includes the current file for graphics paths.
                graphicsPaths.addAll(graphicsPathsInFile(includingCommand.containingFile))

                // Find files/commands to search next
                val file = includingCommand.containingFile
                if (file !in handledFiles) {
                    val commandsIncludingThisFile = allIncludeCommands.filter { includeCommand -> includeCommand.requiredParameters.any { it.contains(file.name) } }
                    newIncludingCommands.addAll(commandsIncludingThisFile)
                    handledFiles.add(file)
                }
            }

            includingCommands = newIncludingCommands
            counter++
        }

        return graphicsPaths
    }

    /**
     * Get all the graphics paths defined by one \graphicspaths command.
     */
    private fun LatexCommands.getGraphicsPaths(): List<String> {
        if (name != "\\graphicspath") return emptyList()
        return parameterList.firstNotNullOfOrNull { it.requiredParam }
            // Each graphics path is in a group.
            ?.childrenOfType(LatexNormalText::class)
            ?.map { it.text }
            // Relative paths (not starting with /) have to be appended to the directory of the file of the given command.
            ?.map { if (it.startsWith('/')) it else containingFile.containingDirectory.virtualFile.path + File.separator + it }
            ?: emptyList()
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = true
}