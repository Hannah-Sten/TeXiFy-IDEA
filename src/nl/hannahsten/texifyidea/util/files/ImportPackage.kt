package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.lookupCommandN
import kotlin.math.min

/**
 * When the 'import' package is used, get all possible parent directories where a file included by the current command could hide.
 */
fun getParentDirectoryByImportPaths(command: LatexCommands): List<VirtualFile> {
    checkForAbsolutePath(command)?.let { return listOf(it) }

    val relativeSearchPaths = mutableListOf<String>()
    if (command.name in CommandMagic.relativeImportCommands) {
        relativeSearchPaths.add(command.requiredParameterText(0) ?: "")
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
        val absolutePath = command.requiredParameterText(0)
        if (absolutePath != null) {
            // No need to search further, because using an absolute path overrides the rest
            LocalFileSystem.getInstance().findFileByPath(absolutePath)?.let { return it }
        }
    }

    return null
}

fun findRelativeSearchPathsForImportCommands(command: LatexCommands, givenRelativeSearchPaths: List<String> = listOf("")): List<VirtualFile> {
    var relativeSearchPaths = givenRelativeSearchPaths.toMutableSet()
    val allIncludeCommands = NewSpecialCommandsIndex.getAllFileInputs(command.project)
    // Commands which may include the current file (this is an overestimation, better would be to check for RequiredFileArguments)
    var includingCommands = allIncludeCommands.filter { includeCommand ->
        includeCommand.requiredParametersText().any { it.contains(command.containingFile.name.removeFileExtension()) }
    }.filter { includeCommand ->
        // Only consider commands that can include LaTeX files
        AllPredefined.lookupCommandN(includeCommand.nameWithoutSlash)?.arguments?.any {
            it.isRequired && LatexContexts.asFileInputCtx(it.contextSignature)?.supportedExtensions?.contains("tex") == true
        } == true
    }
    // To avoid infinite loops, keep track of where we have been
    val visitedIncludeCommands = includingCommands.toMutableSet()
    val handledFiles = mutableListOf<PsiFile>()

    // Assume the containingFile might be a root file - if there are no includingCommands we will not search further anyway
    val defaultParentDir = command.containingFile.containingDirectory?.virtualFile?.findFileByRelativePath(command.requiredParameterText(0) ?: "")

    // Avoid endless loop (in case of a file inclusion loop), limited to some reasonable maximum nesting level
    val maxDepth = min(allIncludeCommands.size, 20)
    var counter = 0

    // Final paths
    val absoluteSearchDirs = mutableListOf<VirtualFile>()

    if (defaultParentDir != null) {
        absoluteSearchDirs.add(defaultParentDir)
    }

    // I think it's a kind of reversed BFS
    // Loop over all commands which possibly include the current element
    while (includingCommands.isNotEmpty() && counter < maxDepth) {
        counter++
        val newSearchPaths = mutableSetOf<String>()
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
                    newSearchPaths.add(includingCommand.requiredParameterText(0) + oldPath)
                }
            }

            // Find files/commands to search next
            val file = includingCommand.containingFile
            if (file !in handledFiles) {
                handledFiles.add(file)
                val commandsIncludingThisFile = allIncludeCommands.filter { includeCommand -> includeCommand.requiredParametersText().any { it.contains(file.name) } }
                if (commandsIncludingThisFile.isEmpty()) {
                    // Cool, we supposedly have a root file, now try to find the directory containing the file being included by command
                    for (relativePath in newSearchPaths) {
                        val searchDir = file.containingDirectory?.virtualFile?.findFileByRelativePath(relativePath) ?: continue
                        absoluteSearchDirs.add(searchDir)
                    }
                }
                newIncludingcommands.addAll(commandsIncludingThisFile)
            }
        }

        val unvisitedNewIncludingCommands = newIncludingcommands.filter { !visitedIncludeCommands.contains(it) }
        includingCommands = unvisitedNewIncludingCommands
        visitedIncludeCommands.addAll(unvisitedNewIncludingCommands)
        relativeSearchPaths = newSearchPaths
    }

    return absoluteSearchDirs
}