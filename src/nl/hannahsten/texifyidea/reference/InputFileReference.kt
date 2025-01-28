package nl.hannahsten.texifyidea.reference

import com.intellij.execution.RunManager
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.algorithm.BFS
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.requiredParameter

/**
 * Reference to a file, based on the command and the range of the filename within the command text.
 */
class InputFileReference(
    element: LatexCommands,
    val range: TextRange,
    val extensions: List<String>,
    val supportsAnyExtension: Boolean,
) : PsiReferenceBase<LatexCommands>(element) {

    init {
        rangeInElement = range
    }

    companion object {

        /**
         * Handle element rename, but taking into account whether the given
         * newElementName is just a filename which we have to replace,
         * or a full relative path (in which case we replace the whole path).
         */
        fun handleElementRename(command: LatexCommands, newElementName: String, elementNameIsJustFilename: Boolean, key: String, range: TextRange): PsiElement {
            // A file has been renamed and we are given a new filename, to be replaced in the parameter text of the current command
            val oldNode = command.node

            val newName = if ((oldNode?.psi as? LatexCommands)?.name in CommandMagic.illegalExtensions.keys) {
                newElementName.removeFileExtension()
            }
            else {
                newElementName
            }

            // Recall that \ is a file separator on Windows
            val newKey = if (elementNameIsJustFilename) key.replaceAfterLast('/', newName, newName) else newName

            // The file may be in a required or optional parameter
            val newText = command.text.replaceRange(range.toIntRange(), newKey)

            val newNode = LatexPsiHelper(command.project).createFromText(newText).firstChild.node ?: return command
            if (oldNode == null) {
                command.parent?.node?.addChild(newNode)
            }
            else {
                command.parent.node.replaceChild(oldNode, newNode)
            }
            return command
        }
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
        val rootFiles = if (givenRootFile != null) setOf(givenRootFile) else element.containingFile.findRootFiles()
            // If the current file is a root file, then we assume paths have to be relative to this file. In particular, when using subfiles then parts that are relative to one of the other root files should not be valid
            .let { if (element.containingFile in it) listOf(element.containingFile) else it }
            .mapNotNull { it.virtualFile }
        val rootDirectories = rootFiles.mapNotNull { it.parent }

        // Check environment variables
        searchPaths += getTexinputsPaths(element.project, rootFiles, expandPaths = true, latexmkSearchDirectory = element.containingFile?.virtualFile?.parent)

        // BIBINPUTS
        // Not used for building the fileset, so we can use the fileset to lookup the BIBINPUTS environment variable
        if (!isBuildingFileset && (element.name in CommandMagic.bibliographyIncludeCommands || extensions.contains("bib"))) {
            // todo check bibtex steps
//            val bibRunConfigs = element.containingFile.getBibtexRunConfigurations()
//            if (bibRunConfigs.any { config -> config.environmentVariables.envs.keys.any { it == "BIBINPUTS" } }) {
//                // When using BIBINPUTS, the file will only be sought relative to BIBINPUTS
//                searchPaths.clear()
//                searchPaths.addAll(bibRunConfigs.mapNotNull { it.environmentVariables.envs["BIBINPUTS"] })
//            }
        }

        // Overrides the default for commands from the graphicx package
        val extensions = if (!isBuildingFileset) {
            val command = LatexCommand.lookup(element.name)?.firstOrNull()
            if (command?.dependency == LatexPackage.GRAPHICX) {
                // We cannot use the file set at this point, so we take the first command in the project and hope for the best
                LatexCommandsIndex.Util.getCommandsByName(LatexGenericRegularCommand.DECLAREGRAPHICSEXTENSIONS.command, element.project, GlobalSearchScope.projectScope(element.project))
                    .firstOrNull()
                    ?.requiredParameter(0)
                    ?.split(",")
                    // Graphicx requires the dot to be included
                    ?.map { it.trim(' ', '.') } ?: extensions
            }
            else {
                extensions
            }
        }
        else {
            extensions
        }

        var processedKey = expandCommandsOnce(key, element.project, file = rootFiles.firstOrNull()?.psiFile(element.project)) ?: key
        // Leading and trailing whitespaces seem to be ignored, at least it holds for \include-like commands
        processedKey = processedKey.trim()

        var targetFile: VirtualFile? = null

        // Try to find the target file directly from the given path
        @Suppress("KotlinConstantConditions")
        if (targetFile == null) {
            for (rootDirectory in rootDirectories) {
                targetFile = rootDirectory.findFile(filePath = processedKey, extensions, supportsAnyExtension)
                if (targetFile != null) break
            }
        }

        // Try content roots, also for non-MiKTeX situations to allow using this as a workaround in case references can't be resolved the regular way
        if (targetFile == null) {
            for (moduleRoot in ProjectRootManager.getInstance(element.project).contentSourceRoots) {
                targetFile = moduleRoot.findFile(processedKey, extensions, supportsAnyExtension)
                if (targetFile != null) break
            }
        }

        // Try graphicspaths
        if (targetFile == null) {
            // If we are not building the fileset, we can make use of it
            if (!isBuildingFileset) {
                val includedPackages = element.containingFile.includedPackages()
                if (CommandMagic.graphicPathsCommands.any { includedPackages.contains(it.dependency) }) {
                    // Add the graphics paths to the search paths
                    searchPaths.addAll(LatexGraphicsPathProvider().getGraphicsPathsInFileSet(element.containingFile))
                }
            }
            for (searchPath in searchPaths) {
                val path = if (!searchPath.endsWith("/")) "$searchPath/" else searchPath
                for (rootDirectory in rootDirectories) {
                    targetFile = rootDirectory.findFile(path + processedKey, extensions, supportsAnyExtension)
                    if (targetFile != null) break
                }
                if (targetFile != null) break
            }
        }

        // Look for packages/files elsewhere using the kpsewhich command.
        if (targetFile == null && lookForInstalledPackages && !element.project.isTestProject()) {
            targetFile = element.getFileNameWithExtensions(processedKey)
                .mapNotNull { LatexPackageLocationCache.getPackageLocation(it, element.project) }
                .firstNotNullOfOrNull { findFileByPath(it) }
        }

        if (targetFile == null) targetFile = searchFileByImportPaths(element)?.virtualFile

        // \externaldocument uses the .aux file in the output directory, we are only interested in the source file, but it can be anywhere (because no relative path will be given, as in the output directory everything will be on the same level).
        // This does not count for building the file set, because the external document is not actually in the fileset, only the label definitions are
        if (!isBuildingFileset && targetFile == null && element.name == LatexGenericRegularCommand.EXTERNALDOCUMENT.commandWithSlash) {
            targetFile = findAnywhereInProject(processedKey)
        }

        // addtoluatexpath package
        if (targetFile == null) {
            for (path in addToLuatexPathSearchDirectories(element.project)) {
                targetFile = path.findFile(processedKey, extensions, supportsAnyExtension)
                if (targetFile != null) break
            }
        }

        if (targetFile == null) return null

        // Return a reference to the target file.
        return PsiManager.getInstance(element.project).findFile(targetFile)
    }

    /**
     * Try to find the file anywhere in the project. Returns the first match.
     * Might be expensive for large projects because of recursively visiting all directories, not sure.
     */
    fun findAnywhereInProject(fileName: String): VirtualFile? {
        val basePath = if (ApplicationManager.getApplication().isUnitTestMode.not()) {
            LocalFileSystem.getInstance().findFileByPath(element.project.basePath ?: return null) ?: return null
        }
        else {
            element.containingFile.virtualFile.parent ?: return null
        }
        BFS(basePath, { file -> file.children.toList() }).apply {
            iterationAction = { file: VirtualFile ->
                if (file.nameWithoutExtension == fileName && file.extension in extensions) {
                    BFS.BFSAction.ABORT
                }
                else {
                    BFS.BFSAction.CONTINUE
                }
            }
            execute()
            return end
        }
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        return handleElementRename(element, newElementName, true, key, range)
    }

    // Required for moving referenced files
    override fun bindToElement(givenElement: PsiElement): PsiElement {
        val newFile = givenElement as? PsiFile ?: return this.element
        // Assume LaTeX will accept paths relative to the root file
        val newFileName = newFile.virtualFile?.path?.toRelativePath(this.element.containingFile.findRootFile().virtualFile.parent.path) ?: return this.element
        return handleElementRename(element, newFileName, false, key, range)
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