package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.isDefinition

/**
 * Finds all the files in the project that are somehow related using includes.
 *
 * When A includes B and B includes C then A, B & C will all return a set containing A, B & C.
 *
 * Be careful when using this function directly over something like [ReferencedFileSetService] where the result
 * values are cached.
 *
 * @param baseFile
 *         The file to find the reference set of.
 * @return All the files that are cross referenced between each other.
 */
// Internal because only ReferencedFileSetCache should call this
internal fun findReferencedFileSetWithoutCache(baseFile: PsiFile): Set<PsiFile> {
    // Setup.
    val project = baseFile.project
    val includes = LatexIncludesIndex.getItems(project)

    // Find all root files.
    val roots = includes.asSequence()
            .map { it.containingFile }
            .distinct()
            .filter { it.isRoot() }
            .toSet()

    // Map root to all directly referenced files.
    val sets = HashMap<PsiFile, Set<PsiFile>>()
    for (root in roots) {
        val referenced = root.referencedFiles(root.virtualFile) + root

        if (referenced.contains(baseFile)) {
            return referenced + baseFile
        }

        sets[root] = referenced
    }

    // Look for matching root.
    for (referenced in sets.values) {
        if (referenced.contains(baseFile)) {
            return referenced + baseFile
        }
    }

    return setOf(baseFile)
}

/**
 * Finds all the files in the project that are somehow related using includes.
 *
 * When A includes B and B includes C then A, B & C will all return a set containing A, B & C.
 *
 * @return All the files that are cross referenced between each other.
 */
fun PsiFile.referencedFileSet(): Set<PsiFile> {
    return ReferencedFileSetService.getInstance().referencedFileSetOf(this)
}

/**
 * @see [BibtexEntryIndex.getIndexedEntriesInFileSet]
 */
fun PsiFile.bibtexIdsInFileSet() = BibtexEntryIndex.getIndexedEntriesInFileSet(this)

/**
 * @see [LatexCommandsIndex.getItemsInFileSet]
 */
fun PsiFile.commandsInFileSet(): Collection<LatexCommands> = LatexCommandsIndex.getItemsInFileSet(this)

/**
 * @see [LatexCommandsIndex.getItemsAndFilesInFileSet]
 */
fun PsiFile.commandsAndFilesInFileSet(): List<Pair<PsiFile, Collection<LatexCommands>>> = LatexCommandsIndex.getItemsAndFilesInFileSet(this)

/**
 * Get all the definitions in the file set.
 */
fun PsiFile.definitionsInFileSet(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItemsInFileSet(this)
            .filter { it.isDefinition() }
}

/**
 * Get all the definitions and redefinitions in the file set.
 */
fun PsiFile.definitionsAndRedefinitionsInFileSet(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItemsInFileSet(this)
}

/**
 * When using \includegraphics from graphicx package, a path prefex can be set with \graphicspath.
 * @return Graphicspaths defined in the fileset.
 */
fun getGraphicsPaths(project: Project): List<String> {

    val graphicsPaths = mutableListOf<String>()
    val graphicsPathCommands = LatexCommandsIndex.getItemsByName("\\graphicspath", project, GlobalSearchScope.projectScope(project))

    // Is a graphicspath defined?
    if (graphicsPathCommands.isNotEmpty()) {
        // Only last defined one counts
        val args = graphicsPathCommands.last().parameterList.filter { it.requiredParam != null }
        val subArgs = args.first().childrenOfType(LatexNormalText::class)
        subArgs.forEach { graphicsPaths.add(it.text) }
    }

    return graphicsPaths
}

/**
 * This method will try to find a file when the 'import' package is used, which means that including files have to be searched for import paths.
 */
fun searchFileByImportPaths(command: LatexCommands): PsiFile? {
    val fileName = if (command.requiredParameters.size >= 2) command.requiredParameters[1] else null ?: return null
    val possibleParentDirs = getParentDirectoryByImportPaths(command)

    for (parentDir in possibleParentDirs) {
        val fileMaybe = parentDir.findFileByRelativePath(fileName)
        if (fileMaybe != null) {
            return fileMaybe.psiFile(command.project)
        }
    }

    return null
}

/**
 * When the 'import' package is used, get all possible parent directories where a file included by the current command could hide.
 */
fun getParentDirectoryByImportPaths(command: LatexCommands): List<VirtualFile> {
    val absoluteImportCommands = setOf("\\includefrom", "\\inputfrom", "\\import")
    val relativeImportCommands = setOf("\\subimport", "\\subinputfrom", "\\subincludefrom")

    // Check if import commands are used (do this now, to only search for import paths when needed)
    val allRelativeImportCommands = LatexIncludesIndex.getCommandsByNames(relativeImportCommands, command.project, GlobalSearchScope.projectScope(command.project))
    val allAbsoluteImportCommands = LatexIncludesIndex.getCommandsByNames(absoluteImportCommands, command.project, GlobalSearchScope.projectScope(command.project))
    if (allAbsoluteImportCommands.isEmpty() && allRelativeImportCommands.isEmpty()) {
        return emptyList()
    }

    // If using an absolute path to include a file
    if (command.name in absoluteImportCommands) {
        val absolutePath = command.requiredParameters.firstOrNull()
        if (absolutePath != null) {
            // No need to search further, because using an absolute path overrides the rest
            val dirMaybe = LocalFileSystem.getInstance().findFileByPath(absolutePath)
            if (dirMaybe != null) {
                return listOf(dirMaybe)
            }
        }
        return emptyList()
    }

    var relativeSearchPaths = mutableListOf<String>()
    if (command.name in relativeImportCommands) {
        relativeSearchPaths.add(command.requiredParameters.firstOrNull() ?: "")
    }

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
            if (includingCommand.name in absoluteImportCommands) {
                val absolutePath = includingCommand.requiredParameters.firstOrNull()
                if (absolutePath != null) {
                    // No need to search further, because using an absolute path overrides the rest
                    val dirMaybe = LocalFileSystem.getInstance().findFileByPath(absolutePath)
                    if (dirMaybe != null) {
                        return listOf(dirMaybe)
                    }
                }
                return emptyList()
            }

            // Each of the search paths gets prepended by one of the new relative paths found
            for (oldPath in relativeSearchPaths) {
                if (includingCommand.name in relativeImportCommands) {
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