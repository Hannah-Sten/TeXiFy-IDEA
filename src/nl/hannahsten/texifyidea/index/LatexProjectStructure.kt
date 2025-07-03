package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.files.documentClassFileInProject
import nl.hannahsten.texifyidea.util.files.findRootFiles
import nl.hannahsten.texifyidea.util.files.referencedFileSet
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
    fun getFilesetsForFile(file: VirtualFile): Set<Fileset> {
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
    // TODO: cache the results of these methods, so that we do not have to recompute them every time, which is comparatively expensive

    /**
     * This is only a reference and will be removed
     */
    private fun buildLatexSearchFiles(baseFile: PsiFile): GlobalSearchScope {
        val useIndexCache = true
        val searchFiles = baseFile.referencedFileSet(useIndexCache)
            .mapNotNullTo(mutableSetOf()) { it.virtualFile }
        searchFiles.add(baseFile.virtualFile)

        // Add document classes
        // There can be multiple, e.g., in the case of subfiles, in which case we probably want all items in the super-fileset
        val roots = baseFile.findRootFiles()
        for (root in roots) {
            val docClass = root.documentClassFileInProject() ?: continue
            searchFiles.add(docClass.virtualFile)
            docClass.referencedFileSet(useIndexCache).forEach {
                searchFiles.add(it.virtualFile)
            }
        }

        // Search index.
//        return GlobalSearchScope.filesScope(baseFile.project, searchFiles)
        TODO()
    }

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
        return root.parent.findFileByRelativePath(relPath.invariantSeparatorsPathString)
    }

    fun getIncludedPackages(file: PsiFile): Set<String> {
        val project = file.project
        return getIncludedPackages(project, buildFilesetScopeFor(file, project))
    }

    fun getIncludedPackages(project: Project, scope: GlobalSearchScope): Set<String> {
        return NewSpecialCommandsIndex.getAllPackageIncludes(project, scope).flatMap {
            it.collectSubtreeTyped<LatexParameterText>().map { it.text }
        }.toSet()
    }

    fun getReferredFiles(
        project: Project,
        file: VirtualFile, root: VirtualFile
    ): Set<VirtualFile> {
        val psiFile = file.findPsiFile(project) ?: return emptySet()
        val fileInputCommands = CachedValuesManager.getCachedValue(psiFile) {
            Result.create(NewSpecialCommandsIndex.getAllFileInputs(project, file), file)
        }
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
        return CachedValuesManager.getManager(project).getCachedValue(project) {
            val roots = getPossibleRootFiles(project)
            val filesets = mutableSetOf<Fileset>()
            val mapping = mutableMapOf<VirtualFile, MutableSet<Fileset>>()
            for (root in roots) {
                val files = mutableSetOf(root)
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
            val projectFilesets = LatexProjectFilesets(filesets, mapping)
            Result.create(projectFilesets, PsiModificationTracker.MODIFICATION_COUNT) // dependency on the whole project structure
        }
    }

    fun findFilesetsFor(psiFile: PsiFile): Set<Fileset> {
        val virtualFile = psiFile.virtualFile ?: return emptySet()
        val project = psiFile.project
        return CachedValuesManager.getManager(project).getCachedValue(psiFile) {
            val filesets = buildFilesets(project).getFilesetsForFile(virtualFile)
            val dependencies = filesets.flatMapTo(mutableSetOf(virtualFile)) { it.files }.toSet()
            Result.create(filesets, dependencies)
        }
    }

    fun buildFilesetScopeFor(file: PsiFile, project: Project = file.project): GlobalSearchScope {
        return CachedValuesManager.getManager(project).getCachedValue(file) {
            val virtualFile = file.virtualFile ?: return@getCachedValue Result.createSingleDependency(GlobalSearchScope.fileScope(file), file)
            val filesets = buildFilesets(project).getFilesetsForFile(virtualFile)
            val allFiles = filesets.flatMapTo(mutableSetOf(virtualFile)) { it.files }
            val result = GlobalSearchScope.filesWithoutLibrariesScope(project, allFiles)
            Result.create(result, allFiles)
        }
    }
}