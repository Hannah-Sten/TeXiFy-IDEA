package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import java.io.File

/**
 * Autocompletion roots based on graphicspaths.
 */
object LatexGraphicsPathProvider : LatexContextAwarePathProviderBase() {

    override fun selectScanRoots(parameters: CompletionParameters): ArrayList<VirtualFile> {
        val paths = getProjectRoots(parameters)
        val file = parameters.originalFile
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