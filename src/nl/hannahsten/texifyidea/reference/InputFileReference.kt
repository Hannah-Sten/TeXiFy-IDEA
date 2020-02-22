package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.LatexDistribution
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.getExternalFile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Reference to a file, based on the command and the range of the filename within the command text.
 *
 * @author Abby Berkers
 */
class InputFileReference(element: LatexCommands, val range: TextRange, val extensions: Set<String>) : PsiReferenceBase<LatexCommands>(element) {
    init {
        rangeInElement = range
    }

    val key by lazy {
        rangeInElement.substring(element.text)
    }

    override fun resolve(): PsiElement? {
        // Get a list of extra paths to search in for the file, absolute or relative
        val searchPaths = if (element.name == "\\includegraphics") {
            getGraphicsPaths()
        }
        else {
            emptyList()
        }

        // todo add search paths, in case of \input or \include, from including files which use \import-like commands

        // Find the sources root of the current file.
        val root = element.containingFile.findRootFile()
                .containingDirectory.virtualFile
        // Find the target file, by first searching through the project directory.
        var targetFile = root.findFile(key, extensions)

        // When the file does not exist in the project directory, look for
        // it elsewhere using the kpsewhich command.
        if (targetFile == null) {
            targetFile = element.getFileNameWithExtensions(key)
                    ?.map { runKpsewhich(it) }
                    ?.map { getExternalFile(it ?: return null) }
                    ?.firstOrNull { it != null }
        }

        // Try search paths
        if (targetFile == null) {
            for (searchPath in searchPaths) {
                targetFile = root.findFile(searchPath + key, extensions)
                if (targetFile != null) break
            }
        }

        // Try content roots
        if (targetFile == null && LatexDistribution.isMiktex) {
            for (moduleRoot in ProjectRootManager.getInstance(element.project).contentSourceRoots) {
                targetFile = moduleRoot.findFile(key, extensions)
                if (targetFile != null) break
            }
        }

        if (targetFile == null) return null

        // Return a reference to the target file.
        return PsiManager.getInstance(element.project).findFile(targetFile)
    }

    /**
     * When using \includegraphics from graphicx package, a path prefex can be set with \graphicspath.
     * @return Graphicspaths defined in the fileset.
     */
    private fun getGraphicsPaths(): List<String> {

        val graphicsPaths = mutableListOf<String>()
        val graphicsPathCommands = element.containingFile.commandsInFileSet().filter { it.name == "\\graphicspath" }

        // Is a graphicspath defined?
        if (graphicsPathCommands.isNotEmpty()) {
            // Only last defined one counts
            val args = graphicsPathCommands.last().parameterList.filter { it.requiredParam != null }
            val subArgs = args.first().childrenOfType(LatexNormalText::class)
            subArgs.forEach { graphicsPaths.add(it.text) }
        }

        return graphicsPaths
    }

    /**
     * Create a set possible complete file names (including extension), based on
     * the command that includes a file, and the name of the file.
     */
    private fun LatexCommands.getFileNameWithExtensions(fileName: String): HashSet<String>? {
        val extension: HashSet<String>? = Magic.Command.includeOnlyExtensions[this.commandToken.text]
        return extension?.map { "$fileName.$it" }?.toHashSet()
    }

    companion object {
        private fun runKpsewhich(arg: String): String? = try {
            BufferedReader(InputStreamReader(Runtime.getRuntime().exec(
                    "kpsewhich $arg"
            ).inputStream)).readLine()  // Returns null if no line read.
        }
        catch (e: IOException) {
            null
        }
    }
}