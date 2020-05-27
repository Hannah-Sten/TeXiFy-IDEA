package nl.hannahsten.texifyidea.completion.pathcompletion

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.findVirtualFileByAbsoluteOrRelativePath

/**
 * Autocompletion roots based on graphicspaths.
 */
class LatexGraphicsPathProvider : LatexPathProviderBase() {
    override fun selectScanRoots(file: PsiFile): ArrayList<VirtualFile> {
        val paths = getProjectRoots()

        val rootDirectory = file.findRootFile().containingDirectory.virtualFile
        getGraphicsPaths(file, file.project).forEach {
            paths.add(rootDirectory.findVirtualFileByAbsoluteOrRelativePath(it) ?: return@forEach)
        }

        return paths
    }

    /**
     * When using \includegraphics from graphicx package, a path prefix can be set with \graphicspath.
     * @return Graphicspaths defined in the fileset.
     */
    fun getGraphicsPaths(file: PsiFile, project: Project): List<String> {

        val graphicsPaths = mutableListOf<String>()
        val graphicsPathCommands = file.commandsInFileSet().filter { it.name == "\\graphicspath" }

        // Is a graphicspath defined?
        if (graphicsPathCommands.isNotEmpty()) {
            // Only last defined one counts
            val args = graphicsPathCommands.last().parameterList.filter { it.requiredParam != null }
            // These arguments have to be in a group in a parameter, and a group contains LatexNormalText instead of LatexParameterText
            val subArgs = args.first().childrenOfType(LatexNormalText::class)
            subArgs.forEach { graphicsPaths.add(it.text) }
        }

        return graphicsPaths
    }

    override fun searchFolders(): Boolean = true

    override fun searchFiles(): Boolean = true
}