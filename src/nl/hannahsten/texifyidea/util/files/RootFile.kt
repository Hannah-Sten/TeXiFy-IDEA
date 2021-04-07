package nl.hannahsten.texifyidea.util.files

import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.magicComment
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.childrenOfType

/**
 * Uses the fileset cache to find all root files in the fileset.
 * Note that each root file induces a fileset, so a file could be in multiple filesets.
 */
fun PsiFile.findRootFilesWithoutCache(fileset: Set<PsiFile>): Set<PsiFile> {
    val magicComment = magicComment()
    val roots = mutableSetOf<PsiFile>()

    if (magicComment.contains(DefaultMagicKeys.ROOT)) {
        val path = magicComment.value(DefaultMagicKeys.ROOT) ?: ""
        this.findFile(path)?.let { roots.add(it) }
    }

    if (this.isRoot()) {
        roots.add(this)
    }

    // Go through the fileset of this file to find out the root files
    for (file in fileset) {
        // Function to avoid unnecessary evaluation
        fun usesSubFiles() = file.commandsInFile()
            .find { it.name == "\\documentclass" }
            ?.requiredParameters
            ?.contains("subfiles") == true

        // Theoretically, we might now add a root file which is included by [this], but in that case we got the root file incorrect anyway
        if (file.isRoot() && !usesSubFiles()) {
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
    val isMainFileInAnyConfiguration = project.getLatexRunConfigurations().any { it.mainFile == this.virtualFile }

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
