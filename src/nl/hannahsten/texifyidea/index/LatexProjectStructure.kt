package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString

// TODO: Fileset service with cache, finalize the representation of filesets
// May be modified in the future
/**
 * A fileset is a set of files that are related to each other, e.g. a main file and its included files (including itself).
 *
 * When resolving subfiles, we must use the relative path from the root file rather than the file that contains the input command.
 */
data class Fileset(
    val files: Set<VirtualFile>,
    val root: VirtualFile,
)

data class LatexProjectFilesets(
    val filesets: Set<Fileset>,
    val mapping: Map<VirtualFile, Set<Fileset>>,
) {
    fun getFilesetForFile(file: VirtualFile): Set<Fileset> {
        return mapping[file] ?: emptySet()
    }
}

fun pathOrNull(pathText: String): Path? {
    return try {
        Path(pathText)
    }
    catch (e: InvalidPathException) {
        null
    }
}

object LatexProjectStructure {

    /**
     *
     */
    fun resolveReferredFile(root: VirtualFile, commandName: String, pathText: String): VirtualFile? {
        var relPath = pathOrNull(pathText) ?: return null
        if (relPath.extension.isEmpty()) {
            val extension = CommandMagic.includeOnlyExtensions[commandName]?.first()
            if (extension != null) {
                // add the extension if it is not present
                relPath = relPath.resolveSibling(relPath.fileName.pathString + ".$extension")
            }
        }
        return root.findFileByRelativePath(relPath.invariantSeparatorsPathString)
    }

    fun getIncludedPackages(file: PsiFile): Set<String> {
        val project = file.project
        return getIncludedPackages(project, buildFilesetScope(project, file))
    }

    fun getIncludedPackages(project: Project, scope: GlobalSearchScope): Set<String> {
        return NewSpecialCommandsIndex.getAllPackageIncludes(project, scope).flatMap {
            it.collectSubtreeTyped<LatexParameterText>().map { it.text }
        }.toSet()
    }

    fun getReferredFiles(project: Project, file: VirtualFile, root: VirtualFile): Set<VirtualFile> {
        val fileInputCommands = NewSpecialCommandsIndex.getAllFileInputs(project, file)
        val result = mutableSetOf<VirtualFile>()
        for (command in fileInputCommands) {
            val commandName = command.name ?: continue
            command.requiredParametersText().forEach { fileName ->
                val vf = resolveReferredFile(root, commandName, fileName)
                if (vf != null) result.add(vf)
            }
        }
        return result
    }

    fun getPossibleRootFiles(project: Project): Set<VirtualFile> {
        if (DumbService.isDumb(project)) return emptySet()
        val documentCommands = NewCommandsIndex.getByName("\\documentclass", project)
        return documentCommands.filter { it.requiredParameterText(0) != null }
            .mapNotNullTo(mutableSetOf()) { it.containingFile.virtualFile }
    }

    fun buildFilesets(project: Project): LatexProjectFilesets {
        val roots = getPossibleRootFiles(project)
        val filesets = mutableSetOf<Fileset>()
        val mapping = mutableMapOf<VirtualFile, MutableSet<Fileset>>()
        for (root in roots) {
            val files = mutableSetOf<VirtualFile>()
            val fileSet = Fileset(files, root)
            val pending = mutableListOf(root)
            while (pending.isNotEmpty()) {
                val f = pending.removeLast()
                mapping.computeIfAbsent(f) { mutableSetOf() }.add(fileSet)
                val referred = getReferredFiles(project, f, root)
                referred.forEach {
                    if (files.add(it)) pending.add(it) // new element added
                }
            }
            filesets.add(fileSet)
        }
        return LatexProjectFilesets(filesets, mapping)
    }

    fun findFileset(project: Project, file: VirtualFile): Set<Fileset> {
        return buildFilesets(project).getFilesetForFile(file)
    }

    fun buildFilesetScope(project: Project, file: PsiFile): GlobalSearchScope {
        val virtualFile = file.virtualFile ?: return GlobalSearchScope.fileScope(file)
        val allFiles = findFileset(project, virtualFile).flatMapTo(mutableSetOf(virtualFile)) { it.files }
        return GlobalSearchScope.filesWithoutLibrariesScope(project, allFiles)
    }
}