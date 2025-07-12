package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.SUBFILES
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.magicComment
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.traverseTyped

/**
 * Find any root files based on information from the current file, e.g. magic comments.
 *
 * Note that each root file induces a fileset, so a file could be in multiple filesets.
 */
suspend fun PsiFile.findRootFilesWithoutCache(): Set<PsiFile> {
    val magicComment = smartReadAction(project) { magicComment() }
    val roots = mutableSetOf<PsiFile>()

    if (magicComment.contains(DefaultMagicKeys.ROOT)) {
        val path = magicComment.value(DefaultMagicKeys.ROOT) ?: ""
        smartReadAction(project) { this.findFile(path, supportsAnyExtension = true) }?.let { roots.add(it) }
    }

    if (smartReadAction(project) { this.isRoot() }) {
        roots.add(this)
    }

    return roots
}

/**
 * Gets the first file from the root files found by [findRootFilesWithoutCache] which is stored in the cache.
 *
 * As a best guess, get the first of the root files returned by [findRootFiles].
 *
 * Note: LaTeX Files can have more than one * root file, so using [findRootFiles] and explicitly handling the cases of
 * multiple root files is preferred over using [findRootFile].
 */
fun PsiFile.findRootFile(useIndexCache: Boolean = true): PsiFile {
    val allRoots = findRootFiles(useIndexCache)
    // If there are multiple root files, prefer the current one
    return if (this in allRoots) this else allRoots.firstOrNull() ?: this
}

/**
 * Gets the set of files that are the root files of `this` file, using [LatexProjectStructure.getFilesetsFor].
 */
fun PsiFile.findRootFiles(useIndexCache: Boolean = true): Set<PsiFile> {
    val project = this.project
    return LatexProjectStructure.getFilesetsFor(this).mapNotNullTo(mutableSetOf()) { it.root.findPsiFile(project) }
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
    fun documentClass() = this.commandsInFile().find { it.name == LatexGenericRegularCommand.DOCUMENTCLASS.cmd }

    fun documentEnvironment() = this.traverseTyped<LatexEnvironment>().any { it.getEnvironmentName() == DefaultEnvironment.DOCUMENT.environmentName }

    // If the file uses the subfiles documentclass, then it is a root file in the sense that all file inclusions
    // will be relative to this file. Note that it may include the preamble of a different file (using optional parameter of \documentclass)
    fun usesSubFiles() = documentClass()?.requiredParametersText()?.contains(SUBFILES.name) == true

    // Go through all run configurations, to check if there is one which contains the current file.
    // If so, then we assume that the file is compilable and must be a root file.
    val isMainFileInAnyConfiguration = project.getLatexRunConfigurations().any { it.mainFile == this.virtualFile }

    return isMainFileInAnyConfiguration || documentEnvironment() || usesSubFiles()
}

/**
 * Get all required arguments, also if comma separated in a group.
 * e.g. \mycommand{arg1,arg2}{arg3} will return [arg1, arg2, arg3].
 */
fun LatexCommands.getAllRequiredArguments(): List<String>? {
    val required = requiredParametersText()
    if (required.isEmpty()) return null
    return required.flatMap { it.split(',') }
}
