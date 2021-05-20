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
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.expandCommandsOnce
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Reference to a file, based on the command and the range of the filename within the command text.
 *
 * @param defaultExtension Default extension of the command in which this reference is.
 */
class InputFileReference(
        element: LatexCommands,
        val range: TextRange,
        val extensions: Set<String>,
        val defaultExtension: String
) : PsiReferenceBase<LatexCommands>(element) {

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
     * @param lookForInstalledPackages
     *              Whether to look for packages installed elsewhere on the filesystem.
     *              Set to false when it would make the operation too expensive, for example when trying to
     *              calculate the fileset of many files.
     * @param givenRootFile Used to avoid unnecessarily recalculating the root file.
     * @param isBuildingFileset
     *              True if we are building the fileset.
     *              If false we also need to resolve to graphics files. Doing so is really expensive at
     *              the moment (at least until the implementation in LatexGraphicsPathProvider is improved):
     *              for projects with 500 include commands in hundreds of files this can take 10 seconds in total if
     *              you call this function for every include command.
     *              However, note that doing only one resolve is not too expensive at all
     *              (10 seconds divided by 500 commands/resolves) so this is not a problem when doing only one resolve
     *              (if requested by the user).
     */
    fun resolve(lookForInstalledPackages: Boolean, givenRootFile: VirtualFile? = null, isBuildingFileset: Boolean = false): PsiFile? {
        // IMPORTANT In this method, do not use any functionality which makes use of the file set,
        // because this function is used to find the file set so that would cause an infinite loop

        // Get a list of extra paths to search in for the file, absolute or relative (to the directory containing the root file)
        val searchPaths = mutableListOf<String>()

        // Find the sources root of the current file.
        // findRootFile will also call getImportPaths, so that will be executed twice
        val rootFiles = if (givenRootFile != null) setOf(givenRootFile) else element.containingFile.findRootFiles().mapNotNull { it.virtualFile }
        val rootDirectories = rootFiles.mapNotNull { it.parent }

        var targetFile: VirtualFile? = null

        // Check environment variables
        val runManager = RunManagerImpl.getInstanceImpl(element.project) as RunManager
        val texInputPath = runManager.allConfigurationsList
                .filterIsInstance<LatexRunConfiguration>()
                .firstOrNull { it.mainFile in rootFiles }
                ?.getConfigOptions()?.environmentVariables
                ?.envs
                ?.getOrDefault("TEXINPUTS", null)
        if (texInputPath != null) {
            val path = texInputPath.trimEnd(':')
            searchPaths.add(path.trimEnd('/'))
            // See the kpathsea manual, // expands to subdirs
            if (path.endsWith("//")) {
                LocalFileSystem.getInstance().findFileByPath(path.trimEnd('/'))?.let { parent ->
                    searchPaths.addAll(
                            parent.allChildDirectories()
                                    .filter { it.isDirectory }
                                    .map { it.path }
                    )
                }
            }
        }

        // BIBINPUTS
        // Not used for building the fileset, so we can use the fileset to lookup the BIBINPUTS environment variable
        if (!isBuildingFileset && (element.name in CommandMagic.bibliographyIncludeCommands || extensions.contains("bib"))) {
            val bibRunConfigs = element.containingFile.getBibtexRunConfigurations()
            if (bibRunConfigs.any { config -> config.environmentVariables.envs.keys.any { it == "BIBINPUTS" } }) {
                // When using BIBINPUTS, the file will only be sought relative to BIBINPUTS
                searchPaths.clear()
                searchPaths.addAll(bibRunConfigs.mapNotNull { it.environmentVariables.envs["BIBINPUTS"] })
            }
        }

        val processedKey = expandCommandsOnce(key, element.project, file = rootFiles.firstOrNull()?.psiFile(element.project)) ?: key

        // Try to find the target file directly from the given path
        if (targetFile == null) {
            for (rootDirectory in rootDirectories) {
                targetFile = rootDirectory.findFile(filePath = processedKey, extensions = extensions)
                if (targetFile != null) break
            }
        }

        // Try content roots
        if (targetFile == null && LatexSdkUtil.isMiktexAvailable) {
            for (moduleRoot in ProjectRootManager.getInstance(element.project).contentSourceRoots) {
                targetFile = moduleRoot.findFile(processedKey, extensions)
                if (targetFile != null) break
            }
        }

        // Try search paths
        if (targetFile == null) {
            if (!isBuildingFileset) {
                // Add the graphics paths to the search paths
                searchPaths.addAll(LatexGraphicsPathProvider().getGraphicsPathsWithoutFileSet(element))
            }
            for (searchPath in searchPaths) {
                val path = if (!searchPath.endsWith("/")) "$searchPath/" else searchPath
                for (rootDirectory in rootDirectories) {
                    targetFile = rootDirectory.findFile(path + processedKey, extensions)
                    if (targetFile != null) break
                }
                if (targetFile != null) break
            }
        }

        // Look for packages/files elsewhere using the kpsewhich command.
        if (targetFile == null && lookForInstalledPackages) {
            targetFile = element.getFileNameWithExtensions(processedKey)
                    .mapNotNull { LatexPackageLocationCache.getPackageLocation(it, element.project) }
                    .mapNotNull { getExternalFile(it) }
                    .firstOrNull()
        }

        if (targetFile == null) targetFile = searchFileByImportPaths(element)?.virtualFile
        if (targetFile == null) return null

        // Return a reference to the target file.
        return PsiManager.getInstance(element.project).findFile(targetFile)
    }

    /**
     * Handle element rename, but taking into account whether the given
     * newElementName is just a filename which we have to replace,
     * or a full relative path (in which case we replace the whole path).
     */
    fun handleElementRename(newElementName: String, elementNameIsJustFilename: Boolean): PsiElement {

        // A file has been renamed and we are given a new filename, to be replaced in the parameter text of the current command
        // It seems to be problematic to find the old filename we want to replace
        // Since the parameter content may be a path, but we are just given a filename, just replace the filename
        // We guess the filename is after the last occurrence of /
        val oldNode = myElement?.node

        val newName = if ((oldNode?.psi as? LatexCommands)?.name in CommandMagic.illegalExtensions.keys) {
            newElementName.removeFileExtension()
        }
        else {
            newElementName
        }

        val defaultNewText = "${myElement?.name}{$newName}"
        // Assumes that it is the last parameter, but at least leaves the options intact
        val default = oldNode?.text?.replaceAfterLast('{', "$newName}", defaultNewText) ?: defaultNewText

        // Recall that \ is a file separator on Windows
        val newText = if (elementNameIsJustFilename) {
            oldNode?.text?.trimStart('\\')?.replaceAfterLast('/', "$newName}", default.trimStart('\\'))
                    ?.let { "\\" + it } ?: default
        }
        else {
            default
        }
        val newNode = LatexPsiHelper(element.project).createFromText(newText).firstChild.node ?: return myElement
        if (oldNode == null) {
            myElement?.parent?.node?.addChild(newNode)
        }
        else {
            myElement.parent.node.replaceChild(oldNode, newNode)
        }
        return myElement
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        return handleElementRename(newElementName, true)
    }

    // Required for moving referenced files
    override fun bindToElement(element: PsiElement): PsiElement {
        val newFile = element as? PsiFile ?: return this.element
        // Assume LaTeX will accept paths relative to the root file
        val newFileName = newFile.virtualFile?.path?.toRelativePath(this.element.containingFile.findRootFile().virtualFile.parent.path) ?: return this.element
        return handleElementRename(newFileName, false)
    }

    /**
     * Create a set possible complete file names (including extension), based on
     * the command that includes a file, and the name of the file.
     */
    private fun LatexCommands.getFileNameWithExtensions(fileName: String): Set<String> {
        val extension = CommandMagic.includeOnlyExtensions[this.commandToken.text] ?: emptySet()
        return extension.map { "$fileName.$it" }.toSet() + setOf(fileName)
    }
}