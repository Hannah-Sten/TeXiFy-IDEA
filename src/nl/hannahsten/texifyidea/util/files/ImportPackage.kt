package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.requiredParameter
import kotlin.math.min

/**
 * This method will try to find a file when the 'import' package is used, which means that including files have to be searched for import paths.
 *
 * Note that this method cannot use other methods that rely on the fileset, because this method is used in building the fileset.
 */
fun searchFileByImportPaths(command: LatexCommands): PsiFile? {
    // Check if import commands are used (do this now, to only search for import paths when needed)
    if (isImportPackageUsed(command.project)) return null

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

fun isImportPackageUsed(project: Project): Boolean {
    val allRelativeImportCommands = NewCommandsIndex.getByNames(
        CommandMagic.relativeImportCommands,
        project,
    )
    if (allRelativeImportCommands.isNotEmpty()) return true
    val allAbsoluteImportCommands = NewCommandsIndex.getByNames(
        CommandMagic.absoluteImportCommands,
        project,
    )
    return allAbsoluteImportCommands.isNotEmpty()
}

/**
 * When the 'import' package is used, get all possible parent directories where a file included by the current command could hide.
 */
fun getParentDirectoryByImportPaths(command: LatexCommands): List<VirtualFile> {
    checkForAbsolutePath(command)?.let { return listOf(it) }

    val relativeSearchPaths = mutableListOf<String>()
    if (command.name in CommandMagic.relativeImportCommands) {
        relativeSearchPaths.add(command.getRequiredParameters().firstOrNull() ?: "")
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
        val absolutePath = command.getRequiredParameters().firstOrNull()
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
        includeCommand.getRequiredParameters().any { it.contains(command.containingFile.name.removeFileExtension()) }
    }.filter { includeCommand ->
        // Only consider commands that can include LaTeX files
        LatexCommand.lookup(includeCommand.name)?.firstOrNull()?.getArgumentsOf(RequiredFileArgument::class.java)?.any { it.supportedExtensions.contains("tex") } == true
    }
    // To avoid infinite loops, keep track of where we have been
    val visitedIncludeCommands = includingCommands.toMutableSet()
    val handledFiles = mutableListOf<PsiFile>()

    // Assume the containingFile might be a root file - if there are no includingCommands we will not search further anyway
    val defaultParentDir = command.containingFile.containingDirectory?.virtualFile?.findFileByRelativePath(command.requiredParameter(0) ?: "")

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
                    newSearchPaths.add(includingCommand.getRequiredParameters().firstOrNull() + oldPath)
                }
            }

            // Find files/commands to search next
            val file = includingCommand.containingFile
            if (file !in handledFiles) {
                handledFiles.add(file)
                val commandsIncludingThisFile = allIncludeCommands.filter { includeCommand -> includeCommand.getRequiredParameters().any { it.contains(file.name) } }
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