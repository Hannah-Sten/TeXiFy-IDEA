package nl.hannahsten.texifyidea.util.files

import com.intellij.execution.RunManager
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.algorithm.IsChildDFS
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.magicComment
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.allFiles
import nl.hannahsten.texifyidea.util.childrenOfType
import java.util.*

/**
 * Fill the root file cache, so for each file in the project cache the root file.
 * It is important to do this in one go, because this is really expensive for large projects.
 * It will be recalculated when the fileset cache is dropped.
 *
 * @param currentFile File for which we want to know the root file, i.e. the file that triggers the filling of the cache.
 *
 * @return Root files for [currentFile].
 */
fun fillRootFileCache(currentFile: PsiFile): Set<PsiFile> {
    // We have to do this search top-down from the root files,
    // because in order to resolve a certain inclusion deep in the file tree
    // we need to know the root of that file (as the path is relative to the root).

    // Start by finding root files in the project
    val project = currentFile.project
    val rootFiles = project.allFiles(LatexFileType)
            .filter { it.psiFile(project)?.isRoot() == true }
    for (rootFile in rootFiles) {
        val rootPsiFile = rootFile.psiFile(project) ?: continue
        val includes = LatexIncludesIndex.getItems(rootPsiFile)

    }
}

/**
 * Scans all file inclusions and finds the files that are at the base of all inclusions.
 * Note that this can be multiple files.
 *
 * When no file is included, `this` file will be returned.
 */
fun PsiFile.findRootFilesWithoutCache(): Set<PsiFile> {
    // todo refactor such that we do DFS from all roots only once, and while doing that fill the cache which maps each
    //     file to its root file - instead of now, where we go through all file inclusions again for every new file opened


    val magicComment = magicComment()
    val roots = mutableSetOf<PsiFile>()

    if (magicComment.contains(DefaultMagicKeys.ROOT)) {
        val path = magicComment.value(DefaultMagicKeys.ROOT) ?: ""
        this.findFile(path)?.let { roots.add(it) }
    }

    if (this.isRoot()) {
        roots.add(this)
    }

    // We need to scan all file inclusions in the project, because any file could include the current file
    val inclusions = project.allFileInclusions()

    inclusions.forEach { (file, _) ->
        // Function to avoid unnecessary evaluation
        fun usesSubFiles() = file.commandsInFile()
            .find { it.name == "\\documentclass" }
            ?.requiredParameters
            ?.contains("subfiles") == true

        // For each root, IsChildDFS until found.
        if (!file.isRoot() || usesSubFiles()) {
            return@forEach
        }

        // If the root file contains this, we have found the root file
        if (file.contains(this, inclusions)) {
            roots.add(file)
        }
    }

    return if (roots.isEmpty()) setOf(this) else roots
}

/**
 * Gets the first file from the root files found by [findRootFilesWithoutCache] which is stored in the cache.
 *
 * As a best guess, get the first of the root files returned by [findRootFiles].
 *
 * Note: LaTeX Files can have more than one * root file, so using [findRootFiles] and explicitly handling the cases of
 * multiple root files is preferred over using [findRootFile].
 */
fun PsiFile.findRootFile(): PsiFile = findRootFiles().firstOrNull() ?: this

/**
 * Gets the set of files that are the root files of `this` file.
 */
fun PsiFile.findRootFiles(): Set<PsiFile> = ReferencedFileSetService.getInstance().rootFilesOf(this)

/**
 * Checks if the given file is included by `this` file.
 *
 * @param childMaybe
 *              The file to check for if it is a child of this file.
 * @param mapping
 *              Map that maps each psi file to all the files that get included by said file.
 * @return `true` when `childMaybe` is a child of `this` file, `false` otherwise.
 */
private fun PsiFile.contains(childMaybe: PsiFile, mapping: Map<PsiFile, Set<PsiFile>>): Boolean {
    return IsChildDFS(
        this,
        { mapping[it] ?: emptySet() },
        { childMaybe == it }
    ).execute()
}

/**
 * Looks up all include commands, and map each file to all the files it directly includes.
 * This may be an expensive function when a lot of include commands and files are present.
 *
 * @return A map that maps each file to a set of all files that get included by said file. E.g.
 * when `A`↦{`B`,`C`}. Then the files `B` and `C` get included by `A`.
 */
internal fun Project.allFileInclusions(): Map<PsiFile, Set<PsiFile>> {
    val allIncludeCommands = LatexIncludesIndex.getItems(this)

    // Maps every file to all the files it includes.
    val inclusions: MutableMap<PsiFile, MutableSet<PsiFile>> = HashMap()

    // Find all related files.
    for (command in allIncludeCommands) {
        // Find included files
        val declaredIn = command.containingFile

        val includedNames = command.getAllRequiredArguments() ?: continue

        var foundFile = false

        for (includedName in includedNames) {
            // todo this doesn't work deep in the file tree, because we have to resolve relative to the root file
            val referenced = declaredIn.findFile(includedName)
                ?: continue

            foundFile = true

            // When it looks like a file includes itself, we skip it
            if (declaredIn.viewProvider.virtualFile.nameWithoutExtension == includedName) {
                continue
            }

            inclusions.getOrPut(declaredIn) { mutableSetOf() }.add(referenced)
        }

        if (!foundFile) {
            // Check if import package is used
            searchFileByImportPaths(command)?.let {
                inclusions.getOrPut(declaredIn) { mutableSetOf() }.add(it)
            }
        }
    }

    return inclusions
}

/**
 * Checks whether the psi file is a tex document root or not.
 *
 * A document root is where the compilation of a tex file starts.
 *
 * @return `true` if the file is a tex document root, `false` if the file is not a root.
 */
fun PsiFile.isRoot(): Boolean {
    if (fileType != LatexFileType) {
        return false
    }

    // Function to avoid unnecessary evaluation
    fun documentClass() = this.commandsInFile().find { it.commandToken.text == "\\documentclass" }

    fun documentEnvironment() = this.childrenOfType(LatexEnvironment::class).any { it.environmentName == "document" }

    // Whether the document makes use of the subfiles class, in which case it is not a root file
    fun usesSubFiles() = documentClass()?.requiredParameters?.contains("subfiles") == true

    // Go through all run configurations, to check if there is one which contains the current file.
    // If so, then we assume that the file is compilable and must be a root file.
    val runManager = RunManagerImpl.getInstanceImpl(project) as RunManager
    val isMainFileInAnyConfiguration = runManager.allConfigurationsList.filterIsInstance<LatexRunConfiguration>().any { it.mainFile == this.virtualFile }

    return (isMainFileInAnyConfiguration || documentEnvironment()) && !usesSubFiles()
}

/**
 * Get all required arguments, also if comma separated in a group.
 * e.g. \mycommand{arg1,arg2}{arg3} will return [arg1, arg2, arg3].
 */
fun LatexCommands.getAllRequiredArguments(): List<String>? {
    val required = requiredParameters
    if (required.isEmpty()) return null
    return required.flatMap { it.split(',') }
}
