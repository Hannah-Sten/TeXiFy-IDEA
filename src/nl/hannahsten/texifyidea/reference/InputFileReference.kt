package nl.hannahsten.texifyidea.reference

import com.intellij.execution.RunManager
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.run.latex.LatexDistribution
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.files.*
import java.io.File

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
    fun resolve(lookForInstalledPackages: Boolean, givenRootFile: VirtualFile? = null): PsiFile? {

        // IMPORTANT In this method, do not use any functionality which makes use of the file set, because this function is used to find the file set so that would cause an infinite loop

        // Get a list of extra paths to search in for the file, absolute or relative (to the directory containing the root file)
        val searchPaths = if (element.name == "\\includegraphics") {
            getGraphicsPaths(element.project)
        }
        else {
            emptyList()
        }.toMutableList()

        // Find the sources root of the current file.
        // findRootFile will also call getImportPaths, so that will be executed twice
        val rootFile = givenRootFile ?: element.containingFile.findRootFile().virtualFile
        val rootDirectory = rootFile.parent ?: return null

        var targetFile = searchFileByImportPaths(element)?.virtualFile

        // Check environment variables
        val runManager = RunManagerImpl.getInstanceImpl(element.project) as RunManager
        val texInputPath = runManager.allConfigurationsList
                .filterIsInstance<LatexRunConfiguration>()
                .firstOrNull { it.mainFile == rootFile }
                ?.environmentVariables
                ?.envs
                ?.getOrDefault("TEXINPUTS", null)
        if (texInputPath != null) {
            val path = texInputPath.trimEnd(':')
            searchPaths.add(path.trimEnd('/'))
            // See the kpathsea manual, // expands to subdirs
            if (path.endsWith("//")) {
                LocalFileSystem.getInstance().findFileByPath(path.trimEnd('/'))?.let { parent ->
                    searchPaths.addAll(parent.allChildDirectories()
                            .filter { it.isDirectory }
                            .map { it.path })
                }
            }
        }

        // Try to find the target file directly from the given path
        if (targetFile == null) {
            targetFile = rootDirectory.findFile(key, extensions)
        }

        // Try content roots
        if (targetFile == null && LatexDistribution.isMiktexAvailable) {
            for (moduleRoot in ProjectRootManager.getInstance(element.project).contentSourceRoots) {
                targetFile = moduleRoot.findFile(key, extensions)
                if (targetFile != null) break
            }
        }

        // Try search paths
        if (targetFile == null) {
            for (searchPath in searchPaths) {
                val path = if (!searchPath.endsWith("/")) "$searchPath/" else searchPath
                targetFile = rootDirectory.findFile(path + key, extensions)
                if (targetFile != null) break
            }
        }

        // Look for packages elsewhere using the kpsewhich command.
        @Suppress("RemoveExplicitTypeArguments")
        if (targetFile == null && lookForInstalledPackages && Magic.Command.includeOnlyExtensions.getOrDefault(element.name, emptySet<String>()).intersect(setOf("sty", "cls")).isNotEmpty()) {
            targetFile = element.getFileNameWithExtensions(key)
                    ?.map { LatexPackageLocationCache.getPackageLocation(it) }
                    ?.map { getExternalFile(it ?: return null) }
                    ?.firstOrNull { it != null }
        }

        if (targetFile == null) return null

        // Return a reference to the target file.
        return PsiManager.getInstance(element.project).findFile(targetFile)
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        // A file has been renamed and we are given a new filename, to be replaced in the parameter text of the current command
        // It seems to be problematic to find the old filename we want to replace
        // Since the parameter content may be a path, but we are just given a filename, just replace the filename
        // We guess the filename is after the last occurrence of /
        val oldNode = myElement?.node
        val newText = oldNode?.text?.replaceAfterLast(File.separator, "${newElementName}}", "${myElement?.name}{$newElementName}") ?: "${myElement?.name}{$newElementName}"
        val newNode = LatexPsiHelper(element.project).createFromText(newText).firstChild.node ?: return myElement
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
}