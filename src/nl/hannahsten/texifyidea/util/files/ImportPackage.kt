package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.appendExtension


/**
 * This method will try to find a file when the 'import' package is used, which means that including files have to be searched for import paths.
 */
fun searchFileByImportPaths(command: LatexCommands, extensions: Set<String>): PsiFile? {
    val fileName = if (command.requiredParameters.size >= 2) command.requiredParameters[1] else null ?: return null

    getParentDirectoryByImportPaths(command).forEach {parentDir ->
        for (extension in extensions) {
            parentDir.findFileByRelativePath(fileName.appendExtension(extension))?.let { return it.psiFile(command.project) }
        }
    }

    return null
}

/**
 * When the 'import' package is used, get all possible parent directories where a file included by the current command could hide.
 */
fun getParentDirectoryByImportPaths(command: LatexCommands): List<VirtualFile> {
    // Check if import commands are used (do this now, to only search for import paths when needed)
    val allRelativeImportCommands = LatexIncludesIndex.getCommandsByNames(Magic.Command.relativeImportCommands, command.project, GlobalSearchScope.projectScope(command.project))
    val allAbsoluteImportCommands = LatexIncludesIndex.getCommandsByNames(Magic.Command.absoluteImportCommands, command.project, GlobalSearchScope.projectScope(command.project))
    if (allAbsoluteImportCommands.isEmpty() && allRelativeImportCommands.isEmpty()) {
        return emptyList()
    }

    checkForAbsolutePath(command)?.let { return listOf(it) }

    val relativeSearchPaths = mutableListOf<String>()
    if (command.name in Magic.Command.relativeImportCommands) {
        relativeSearchPaths.add(command.requiredParameters.firstOrNull() ?: "")
    }

    return findRelativeSearchPathsForImportCommands(command, relativeSearchPaths)
}

/**
 * If using an absolute path to include a file.
 */
fun checkForAbsolutePath(command: LatexCommands): VirtualFile? {
    if (command.name in Magic.Command.absoluteImportCommands) {
        val absolutePath = command.requiredParameters.firstOrNull()
        if (absolutePath != null) {
            // No need to search further, because using an absolute path overrides the rest
            LocalFileSystem.getInstance().findFileByPath(absolutePath)?.let { return it }
        }
    }

    return null
}

fun findRelativeSearchPathsForImportCommands(command: LatexCommands, givenRelativeSearchPaths: List<String> = listOf()): List<VirtualFile> {
    var relativeSearchPaths = givenRelativeSearchPaths.toMutableList()
    val allIncludeCommands = LatexIncludesIndex.getItems(command.project)
    // Commands which may include the current file (this is an overestimation, better would be to check for RequiredFileArguments)
    var includingCommands = allIncludeCommands.filter { includeCommand -> includeCommand.requiredParameters.any { it.contains(command.containingFile.name) } }

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
            checkForAbsolutePath(command)?.let { return listOf(it) }

            // Each of the search paths gets prepended by one of the new relative paths found
            for (oldPath in relativeSearchPaths) {
                if (includingCommand.name in Magic.Command.relativeImportCommands) {
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
                        val searchDir = LocalFileSystem.getInstance().findFileByPath(file.containingDirectory.virtualFile.path + relativePath) ?: continue
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