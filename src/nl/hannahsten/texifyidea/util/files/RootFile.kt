package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.SUBFILES
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.magicComment
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.psi.childrenOfType
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.magic.cmd

/**
 * Uses the fileset cache to find all root files in the fileset.
 * Note that each root file induces a fileset, so a file could be in multiple filesets.
 */
fun PsiFile.findRootFilesWithoutCache(fileset: Set<PsiFile>): Set<PsiFile> {
    val magicComment = runReadAction { magicComment() }
    val roots = mutableSetOf<PsiFile>()

    if (magicComment.contains(DefaultMagicKeys.ROOT)) {
        val path = magicComment.value(DefaultMagicKeys.ROOT) ?: ""
        this.findFile(path)?.let { roots.add(it) }
    }

    if (this.isRoot()) {
        roots.add(this)
    }

    roots.addAll(fileset.filter { it.isRoot() })

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

    fun documentEnvironment() = this.childrenOfType(LatexEnvironment::class).any { it.environmentName == DefaultEnvironment.DOCUMENT.environmentName }

    // If the file uses the subfiles documentclass, then it is a root file in the sense that all file inclusions
    // will be relative to this file. Note that it may include the preamble of a different file (using optional parameter of \documentclass)
    fun usesSubFiles() = documentClass()?.requiredParameters?.contains(SUBFILES.name) == true

    // Go through all run configurations, to check if there is one which contains the current file.
    // If so, then we assume that the file is compilable and must be a root file.
    val isMainFileInAnyConfiguration = project.getLatexRunConfigurations().any { it.mainFile == this.virtualFile }

    return runReadAction { isMainFileInAnyConfiguration || documentEnvironment() || usesSubFiles() }
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
