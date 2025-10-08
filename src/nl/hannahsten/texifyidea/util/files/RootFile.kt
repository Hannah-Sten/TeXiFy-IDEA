package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * As the best guess, get the first of the root files returned by [findRootFiles].
 *
 * Note: LaTeX Files can have more than one * root file, so using [findRootFiles] and explicitly handling the cases of
 * multiple root files is preferred over using [findRootFile].
 */
fun PsiFile.findRootFile(): PsiFile {
    val allRoots = findRootFiles()
    // If there are multiple root files, prefer the current one
    return if (this in allRoots) this else allRoots.firstOrNull() ?: this
}

/**
 * Gets the set of files that are the root files of `this` file, using [LatexProjectStructure.getFilesetsFor].
 */
fun PsiFile.findRootFiles(): Set<PsiFile> {
    val project = this.project
    return LatexProjectStructure.getFilesetsFor(this).mapNotNullTo(mutableSetOf()) { it.root.findPsiFile(project) }
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
