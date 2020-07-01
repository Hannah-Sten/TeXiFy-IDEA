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
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import java.util.HashMap

/**
 * Scans all file inclusions and finds the file that is at the base of all inclusions.
 *
 * When no file is included, `this` file will be returned.
 */
fun PsiFile.findRootFile(): PsiFile {
    if (magicComment().contains(DefaultMagicKeys.ROOT)) {
        val path = magicComment().value(DefaultMagicKeys.ROOT) ?: ""
        this.findFile(path)?.let { return it }
    }

    if (this.isRoot()) {
        return this
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
private fun PsiFile.contains(childMaybe: PsiFile, mapping: Map<PsiFile, Set<PsiFile>>): Boolean {
    return IsChildDFS(
            this,
            { mapping[it] ?: emptySet() },
            { childMaybe == it }
    ).execute()
}

/**
 * Looks up all the files that include each other.
 * This may be an expensive function when a lot of include commands and files are present.
 *
 * @return A map that maps each file to a set of all files that get included by said file. E.g.
 * when `A`↦{`B`,`C`}. Then the files `B` and `C` get included by `A`.
 */
private fun Project.allFileInclusions(): Map<PsiFile, Set<PsiFile>> {
    val allIncludeCommands = LatexIncludesIndex.getItems(this)

    // Maps every file to all the files it includes.
    val inclusions: MutableMap<PsiFile, MutableSet<PsiFile>> = HashMap()

    // Find all related files.
    for (command in allIncludeCommands) {
        // Find included files
        val declaredIn = command.containingFile

        // Check if import package is used
        val fileMaybe = searchFileByImportPaths(command)
        if (fileMaybe != null) {
            inclusions.getOrPut(declaredIn) { mutableSetOf() }.add(fileMaybe)
        }
        else {
            val includedNames = command.getAllRequiredArguments() ?: continue

            for (includedName in includedNames) {
                val referenced = declaredIn.findFile(includedName)
                        ?: continue

                // When it looks like a file includes itself, we skip it
                if (declaredIn.viewProvider.virtualFile.nameWithoutExtension == includedName) {
                    continue
                }

                inclusions.getOrPut(declaredIn) { mutableSetOf() }.add(referenced)
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

    // Whether the document makes use of the subfiles class, in which case it is not a root file
    fun usesSubFiles() = documentClass()?.requiredParameters?.contains("subfiles") == true

    // Go through all run configurations, to check if there is one which contains the current file.
    // If so, then we assume that the file is compilable and must be a root file.
    val runManager = RunManagerImpl.getInstanceImpl(project) as RunManager
    val isMainFileInAnyConfiguration = runManager.allConfigurationsList.filterIsInstance<LatexRunConfiguration>().any { it.mainFile == this.virtualFile }

    return isMainFileInAnyConfiguration || documentClass() != null && !usesSubFiles()
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
