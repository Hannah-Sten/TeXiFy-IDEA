package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import java.io.File

/**
 * Autocompletion roots based on graphicspaths.
 */
object LatexGraphicsPathProvider : LatexPathProviderBase() {

    override fun selectScanRoots(file: PsiFile): ArrayList<VirtualFile> {
        val paths = getProjectRoots()

        val rootDirectory = file.findRootFile().containingDirectory?.virtualFile ?: return arrayListOf()
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
        val graphicsPathCommands = NewCommandsIndex.getByNamesInFileSet(CommandMagic.graphicPathsCommandNames, file)

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
        fun graphicsPathsInFile(file: PsiFile): List<String> = file.traverseCommands()
            .filter { it.name == "\\graphicspath" }
            .flatMap { it.getGraphicsPaths() }.toList()

        // First find all graphicspaths commands in the file of the given command
        val graphicsPaths = graphicsPathsInFile(command.containingFile).toMutableList()

        val allIncludeCommands = NewSpecialCommandsIndex.getAllFileInputs(command.project)
        // Commands which may include the current file (this is an overestimation, better would be to check for RequiredFileArguments)
        var includingCommands = allIncludeCommands.filter { includeCommand -> includeCommand.requiredParametersText().any { it.contains(command.containingFile.name.removeFileExtension()) } }

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
                    val commandsIncludingThisFile = allIncludeCommands.filter { includeCommand -> includeCommand.requiredParametersText().any { it.contains(file.name) } }
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
    fun LatexCommands.getGraphicsPaths(): List<String> {
        if (!CommandMagic.graphicPathsCommandNames.contains(name)) return emptyList()
        val first = parameterList.firstNotNullOfOrNull { it.requiredParam } ?: return emptyList()
        return first.collectSubtreeTyped<LatexNormalText>().mapNotNull {
            val text = it.text
            if (text.startsWith('/')) text
            else {
                // Relative paths (not starting with /) have to be appended to the directory of the file of the given command.
                it.containingFile?.containingDirectory?.virtualFile?.path?.let { path ->
                    path + File.separator + text
                }
            }
        }
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = true
}