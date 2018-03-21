package nl.rubensten.texifyidea.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import nl.rubensten.texifyidea.algorithm.IsChildDFS
import nl.rubensten.texifyidea.file.ClassFileType
import nl.rubensten.texifyidea.file.LatexFileType
import nl.rubensten.texifyidea.file.StyleFileType
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.lang.Package
import java.util.*
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
object FileUtil {

    /**
     * Matches the extension of a file name, including the dot.
     */
    val FILE_EXTENSION = Pattern.compile("\\.[^.]+$")!!
}

/**
 * Looks up the PsiFile that corresponds to the Virtual File.
 */
fun VirtualFile.psiFile(project: Project): PsiFile? = PsiManager.getInstance(project).findFile(this)

/**
 * Removes the extension from a given file name.
 */
fun String.removeFileExtension() = FileUtil.FILE_EXTENSION.matcher(this).replaceAll("")!!

/**
 * Creates a project directory at `path` which will be marked as excluded.
 *
 * @param path The path to create the directory to.
 */
fun Module.createExcludedDir(path: String) {
    ModuleRootManager.getInstance(this).modifiableModel.addContentEntry(path).addExcludeFolder(path)
}

/**
 * Scans all file inclusions and finds the file that is at the base of all inclusions.
 *
 * When no file is included, `this` file will be returned.
 */
fun PsiFile.findRootFile(): PsiFile {
    if (LatexCommandsIndex.getItems(this).any { "\\documentclass" == it.name }) {
        return this
    }

    val inclusions = project.allFileinclusions()
    inclusions.forEach { (file, _) ->
        // For each root, IsChildDFS until found.
        if (!file.isRoot()) {
            return@forEach
        }

        if (file.isIncludedBy(file, inclusions)) {
            return file
        }
    }

    return this
}

/**
 * Checks if the given file is included by `this` file.
 *
 * @param childMaybe
 *              The file to check for if it is a child of this file.
 * @param mapping
 *              Map that maps each psi file to all the files that get included by said file.
 * @return `true` when `childMaybe` is a child of `this` file, `false` otherwise.
 */
private fun PsiFile.isIncludedBy(childMaybe: PsiFile, mapping: Map<PsiFile, Set<PsiFile>>): Boolean {
    return IsChildDFS<PsiFile>(
            this,
            { mapping[it] ?: emptySet() },
            { childMaybe == it }
    ).execute()
}

/**
 * Looks up all the files that include each other.
 *
 * @return A map that maps each file to a set of all files that get included by said file. E.g.
 * when `A`↦{`B`,`C`}. Then the files `B` and `C` get included by `A`.
 */
fun Project.allFileinclusions(): Map<PsiFile, Set<PsiFile>> {
    val commands = LatexCommandsIndex.getItems(this)

    // Maps every file to all the files it includes.
    val inclusions: MutableMap<PsiFile, MutableSet<PsiFile>> = HashMap()

    // Find all related files.
    for (command in commands) {
        val includedName = TexifyUtil.getIncludedFile(command) ?: continue
        val declaredIn = command.containingFile
        val referenced = TexifyUtil.getFileRelativeTo(declaredIn, includedName, null) ?: continue

        val inclusionSet = inclusions[declaredIn] ?: HashSet()
        inclusionSet.add(referenced)
        inclusions[declaredIn] = inclusionSet
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

    return commandsInFile().filter { it.commandToken.text == "\\documentclass" }.any()
}

/**
 * @see [TexifyUtil.getFileRelativeTo]
 */
fun PsiFile.findRelativeFile(filePath: String, extensions: Set<String>? = null) = TexifyUtil.getFileRelativeTo(this, filePath, extensions)

/**
 * Looks for all file inclusions in a given file.
 *
 * @return A list containing all included files.
 */
fun PsiFile.findInclusions(): List<PsiFile> {
    val root = findRootFile()
    return commandsInFile()
            .filter { "\\input" == it.name || "\\include" == it.name || "\\includeonly" == it.name }
            .map { it.requiredParameter(0) }
            .filter(Objects::nonNull)
            .map { root.findRelativeFile(it!!) }
            .filter(Objects::nonNull)
            .map { it!! }
}

/**
 * Checks if the file has LaTeX syntax.
 */
fun PsiFile.isLatexFile() = fileType == LatexFileType ||
        fileType == StyleFileType || fileType == ClassFileType

/**
 * Checks if the file has a `.sty` extention. This is a workaround for file type checking.
 */
fun PsiFile.isStyleFile() = virtualFile.extension == "sty"

/**
 * Checks if the file has a `.cls` extention. This is a workaround for file type checking.
 */
fun PsiFile.isClassFile() = virtualFile.extension == "cls"

/**
 * Looks up the the that is in the documentclass command.
 */
fun PsiFile.documentClassFile(): PsiFile? {
    val command = commandsInFile().filter { it.name == "\\documentclass" }.getOrNull(0) ?: return null
    val argument = command.requiredParameter(0) ?: return null
    return fileRelativeTo("$argument.cls")
}

/**
 * Checks if the given package is included in the file set.
 *
 * @param packageName
 *          The name of the package to check for.
 * @return `true` when there is a package with name `packageName` in the file set, `false` otherwise.
 */
fun PsiFile.isUsed(packageName: String) = PackageUtils.getIncludedPackages(this).contains(packageName)

/**
 * Checks if the given package is included into the file set.
 *
 * @param `package`
 *          The package to check for.
 * @return `true` when there is a package `package` included in the file set, `false` otherwise.
 */
fun PsiFile.isUsed(`package`: Package) = isUsed(`package`.name)

/**
 * Retrieves the [PsiFile] for the document within the given [project].
 *
 * @param project
 *          The project scope to retrieve the psi file for.
 * @return The PSI file matching the document, or `null` when the PSI file could not be found.
 */
fun Document.psiFile(project: Project): PsiFile? = PsiDocumentManager.getInstance(project).getPsiFile(this)
