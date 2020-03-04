package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.LatexDistribution
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.getExternalFile
import nl.hannahsten.texifyidea.util.files.getGraphicsPaths
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Reference to a file, based on the command and the range of the filename within the command text.
 *
 * @param defaultExtension Default extension of the command in which this reference is.
 *
 * @author Abby Berkers
 */
class InputFileReference(element: LatexCommands, val range: TextRange, val extensions: Set<String>, val defaultExtension: String) : PsiReferenceBase<LatexCommands>(element) {
    init {
        rangeInElement = range
    }

    val key by lazy {
        rangeInElement.substring(element.text)
    }

    override fun resolve(): PsiFile? {
        return resolve(true)
    }

    /**
     * @param lookForInstalledPackages Whether to look for packages installed elsewhere on the filesystem.
     * Set to false when it would make the operation too expensive, for example when trying to calculate the fileset of many files.
     */
    fun resolve(lookForInstalledPackages: Boolean, rootFile: VirtualFile? = null): PsiFile? {

        // IMPORTANT In this method, do not use any functionality which makes use of the file set, because this function is used to find the file set so that would cause an infinite loop

        // Get a list of extra paths to search in for the file, absolute or relative (to the directory containing the root file)
        val searchPaths = if (element.name == "\\includegraphics") {
            getGraphicsPaths(element.project)
        }
        else {
            emptyList()
        }.toMutableList()

        checkImportPaths(searchPaths)

        // Find the sources root of the current file.
        val rootDirectory = rootFile?.parent ?: element.containingFile.findRootFile()
                .containingDirectory.virtualFile

        // Try to find the target file directly from the given path
        var targetFile = rootDirectory.findFile(key, extensions)

        // Try content roots
        if (targetFile == null && LatexDistribution.isMiktex) {
            for (moduleRoot in ProjectRootManager.getInstance(element.project).contentSourceRoots) {
                targetFile = moduleRoot.findFile(key, extensions)
                if (targetFile != null) break
            }
        }

        // Try search paths
        if (targetFile == null) {
            for (searchPath in searchPaths) {
                targetFile = rootDirectory.findFile(searchPath + key, extensions)
                if (targetFile != null) break
            }
        }

        // Look for packages elsewhere using the kpsewhich command.
        @Suppress("RemoveExplicitTypeArguments")
        if (targetFile == null && lookForInstalledPackages && Magic.Command.includeOnlyExtensions.getOrDefault(element.name, emptySet<String>()).intersect(setOf("sty", "cls")).isNotEmpty()) {
            targetFile = element.getFileNameWithExtensions(key)
                    ?.map { runKpsewhich(it) }
                    ?.map { getExternalFile(it ?: return null) }
                    ?.firstOrNull { it != null }
        }

        if (targetFile == null) return null

        // Return a reference to the target file.
        return PsiManager.getInstance(element.project).findFile(targetFile)
    }

    /**
     * Check for search paths from the 'import' package.
     */
    private fun checkImportPaths(searchPaths: MutableList<String>) {
        // If using an absolute path to include a file
        if (element.name in setOf("\\includefrom", "\\inputfrom", "\\import")) {
            val absolutePath = element.requiredParameters.firstOrNull()
            if (absolutePath != null) {
                searchPaths.add(absolutePath)
            }
        }

        // If using a relative path, these could be nested from other inclusions
        val relativePathCommands = setOf("\\subimport", "\\subinputfrom", "\\subincludefrom")
        if (element.name in relativePathCommands) {
            var relativeSearchPaths = listOf(element.requiredParameters.firstOrNull() ?: "")

            // Get all commands in the file set which have a relative import
            val allRelativeIncludeCommands = LatexIncludesIndex.getCommandsByNames(relativePathCommands, element.project, GlobalSearchScope.projectScope(element.project))

            // Navigate upwards
            // Commands which include the current file
            var includingCommands = allRelativeIncludeCommands.filter { command -> command.requiredParameters.size >= 2 && command.requiredParameters[1].contains(element.containingFile.name) }
            var parents = includingCommands
                    .map { it.containingFile }
                    .filter { it != element.containingFile }
                    .toSet()

            // Avoid endless loop (in case of a file inclusion loop)
            val maxDepth = allRelativeIncludeCommands.size
            var counter = 0
            while (parents.isNotEmpty() && counter < maxDepth) {
                val newSearchPaths = mutableListOf<String>()
                for (oldPath in relativeSearchPaths) {
                    // Each of the search paths gets prepended by one of the new relative paths found
                    for (command in includingCommands) {
                        newSearchPaths.add(command.requiredParameters.firstOrNull() + oldPath)
                    }
                }
                relativeSearchPaths = newSearchPaths

                includingCommands = allRelativeIncludeCommands.filter { command -> parents.any { command.requiredParameters.size >= 2 && command.requiredParameters[1].contains(it.name) } }
                parents = includingCommands.map { it.containingFile }.toSet()

                counter++
            }

            searchPaths.addAll(relativeSearchPaths)
        }
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        // A file has been renamed and we are given a new filename, to be replaced in the parameter text of the current command
        // It seems to be problematic to find the old filename we want to replace
        val commandText = "${myElement?.name}{$newElementName}"
        val oldNode = myElement?.node
        val newNode = LatexPsiHelper(element.project).createFromText(commandText).firstChild.node
        if (oldNode == null) {
            myElement?.parent?.node?.addChild(newNode)
        }
        else {
            myElement.parent.node.replaceChild(oldNode, newNode)
        }
        return myElement
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