package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.requiredParameter

/**
 * This method will try to find a file when the 'import' package is used, which means that including files have to be searched for import paths.
 *
 * Note that this method cannot use other methods that rely on the fileset, because this method is used in building the fileset.
 */
fun searchFileByImportPaths(command: LatexCommands): PsiFile? {
    // Check if import commands are used (do this now, to only search for import paths when needed)
    val allRelativeImportCommands = LatexIncludesIndex.getCommandsByNames(
        CommandMagic.relativeImportCommands,
        command.project,
        GlobalSearchScope.projectScope(command.project)
    )
    val allAbsoluteImportCommands = LatexIncludesIndex.getCommandsByNames(
        CommandMagic.absoluteImportCommands,
        command.project,
        GlobalSearchScope.projectScope(command.project)
    )
    if (allAbsoluteImportCommands.isEmpty() && allRelativeImportCommands.isEmpty()) {
        return null
    }

    // Use references to get filenames, take care not to resolve the references because this method is called during resolving them so that would be a loop. This line will take a very long time for large projects, as it has to do a lot of recursive navigation in the psi tree in order to get the text required for building the reference keys.
    val references = command.references.filterIsInstance<InputFileReference>()

    getParentDirectoryByImportPaths(command).forEach { parentDir ->
        for (reference in references) {
            val fileName = reference.key
            for (extension in reference.extensions) {
                parentDir.findFileByRelativePath(fileName.appendExtension(extension))?.let {
                    return it.psiFile(command.project)
                }
            }
        }
    }

    return null
}

/**
 * When the 'import' package is used, get all possible parent directories where a file included by the current command could hide.
 */
fun getParentDirectoryByImportPaths(command: LatexCommands): List<VirtualFile> {
    checkForAbsolutePath(command)?.let { return listOf(it) }

    val relativeSearchPaths = mutableListOf<String>()
    if (command.name in CommandMagic.relativeImportCommands) {
        relativeSearchPaths.add(command.requiredParameters.firstOrNull() ?: "")
    }
    else {
        relativeSearchPaths.add("")
    }

    return findRelativeSearchPathsForImportCommands(command, relativeSearchPaths)
}

/**
 * If using an absolute path to include a file.
 */
fun checkForAbsolutePath(command: LatexCommands): VirtualFile? {
    if (command.name in CommandMagic.absoluteImportCommands) {
        val absolutePath = command.requiredParameters.firstOrNull()
        if (absolutePath != null) {
            // No need to search further, because using an absolute path overrides the rest
            LocalFileSystem.getInstance().findFileByPath(absolutePath)?.let { return it }
        }
    }

    return null
}

fun findRelativeSearchPathsForImportCommands(command: LatexCommands, givenRelativeSearchPaths: List<String> = listOf("")): List<VirtualFile> {
    var relativeSearchPaths = givenRelativeSearchPaths.toMutableList()
    val allIncludeCommands = LatexIncludesIndex.getItems(command.project)
    // Commands which may include the current file (this is an overestimation, better would be to check for RequiredFileArguments)
    var includingCommands = allIncludeCommands.filter { includeCommand -> includeCommand.requiredParameters.any { it.contains(command.containingFile.name.removeFileExtension()) } }

    if (includingCommands.isEmpty() && command.name in CommandMagic.relativeImportCommands) {
        command.containingFile.containingDirectory?.virtualFile?.findFileByRelativePath(command.requiredParameter(0) ?: "")?.let { return listOf(it) }
    }

    // Avoid endless loop (in case of a file inclusion loop)
    val maxDepth = allIncludeCommands.size
    var counter = 0

    // Final paths
    val absoluteSearchDirs = mutableListOf<VirtualFile>()

    // I think it's a kind of reversed BFS
    while (includingCommands.isNotEmpty() && counter < maxDepth) {
        val newSearchPaths = mutableListOf<String>()
        val handledFiles = mutableListOf<PsiFile>()
        val newIncludingcommands = mutableListOf<LatexCommands>()

        for (includingCommand in includingCommands) {
            // Stop searching when absolute path is found
            checkForAbsolutePath(includingCommand)?.let { baseDir ->
                return listOf(baseDir) + relativeSearchPaths.mapNotNull {
                    baseDir.findFileByRelativePath(it)
                }
            }

            // Each of the search paths gets prepended by one of the new relative paths found
            for (oldPath in relativeSearchPaths) {
                if (includingCommand.name in CommandMagic.relativeImportCommands) {
                    newSearchPaths.add(includingCommand.requiredParameters.firstOrNull() + oldPath)
                }
            }

            // Find files/commands to search next
            val file = includingCommand.containingFile
            if (file !in handledFiles) {
                val commandsIncludingThisFile = allIncludeCommands.filter { includeCommand -> includeCommand.requiredParameters.any { it.contains(file.name) } }
                if (commandsIncludingThisFile.isEmpty()) {
                    // Cool, we supposedly have a root file, now try to find the directory containing the file being included by command
                    for (relativePath in newSearchPaths) {
                        val searchDir = file.containingDirectory?.virtualFile?.findFileByRelativePath(relativePath) ?: continue
                        absoluteSearchDirs.add(searchDir)
                    }
                }
                newIncludingcommands.addAll(commandsIncludingThisFile)
                handledFiles.add(file)
            }
        }

        includingCommands = newIncludingcommands
        relativeSearchPaths = newSearchPaths
        counter++
    }

    return absoluteSearchDirs
}