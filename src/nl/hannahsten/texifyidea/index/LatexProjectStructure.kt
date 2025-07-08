package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.TexifyProjectCacheService
import nl.hannahsten.texifyidea.util.files.LatexPackageLocation
import nl.hannahsten.texifyidea.util.getBibtexRunConfigurations
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.getTexinputsPaths
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString

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

data class FilesetData(
    val filesets: Set<Fileset>,
    val relatedFiles: Set<VirtualFile>,
    val filesetScope: GlobalSearchScope,
)

data class LatexProjectFilesets(
    val filesets: Set<Fileset>,
    val mapping: Map<VirtualFile, FilesetData>,
) {
    fun getData(project: Project, file: VirtualFile): FilesetData? {
        return mapping[file]
    }
}

fun pathOrNull(pathText: String?): Path? {
    if (pathText.isNullOrBlank()) return null
    return try {
        Path(pathText)
    }
    catch (e: InvalidPathException) {
        null
    }
}

object LatexProjectStructure {

    const val CACHE_EXPIRATION_IN_MS = 5000L // 5 seconds

    private class BuildFilesetPreparation(
        val project: Project,
        val texInputPaths: Set<Path>,
        val bibInputPaths: Set<Path>,
    )

    /**
     *
     */
    private fun appendReferredFile(
        result: MutableList<VirtualFile>,
        root: VirtualFile, commandName: String, text: String, sp: BuildFilesetPreparation
    ) {
        var path = pathOrNull(text) ?: return

        if (path.extension.isEmpty()) {
            val extension = CommandMagic.includeOnlyExtensions[commandName]?.first()
            if (extension != null) {
                // add the extension if it is not present
                path = path.resolveSibling(path.fileName.pathString + ".$extension")
            }
        }

        fun appendFilesFromSourcePaths(sourcePaths: Set<Path>) {
            for (sourcePath in sourcePaths) {
                val file = sourcePath.resolve(path) ?: continue
                val vf = LocalFileSystem.getInstance().findFileByNioFile(file)
                if (vf != null) {
                    result.add(vf)
                }
            }
        }

        if (commandName in CommandMagic.bibliographyIncludeCommands) {
            // For bibliography files, we can search in the bib input paths
            appendFilesFromSourcePaths(sp.bibInputPaths)
        }
        if (commandName in CommandMagic.packageInclusionCommands) {
            // try to find the file in the tex input paths
            val location = LatexPackageLocation.getPackageLocation(path.pathString, sp.project)
            if (location != null) {
                val vf = LocalFileSystem.getInstance().findFileByNioFile(location)
                if (vf != null) {
                    result.add(vf)
                }
            }
        }
        // plain way
        if (path.isAbsolute) {
            LocalFileSystem.getInstance().findFileByNioFile(path)?.let { result.add(it) }
        }
        else {
            root.parent.findFileByRelativePath(path.invariantSeparatorsPathString)?.let { result.add(it) }
        }
    }

    /**
     * Gets the directly referred files from the given file relative to the given root file.
     *
     * Note: the path of the referred file is relative to the root file, not the file that contains the input command.
     */
    private fun getDirectReferredFiles(
        project: Project,
        file: VirtualFile, root: VirtualFile,
        sp: BuildFilesetPreparation
    ): List<VirtualFile> {
        val psiFile = file.findPsiFile(project) ?: return emptyList()
        val fileInputCommands = CachedValuesManager.getCachedValue(psiFile) {
            Result.create(NewSpecialCommandsIndex.getAllFileInputs(project, file), file)
        }
        val result = mutableListOf<VirtualFile>()
        for (command in fileInputCommands) {
            val commandName = command.name ?: continue
            command.requiredParametersText().forEach { text ->
                text.split(" ").forEach { fileName ->
                    appendReferredFile(result, root, commandName, fileName.trim(), sp)
                }
            }
        }

        return result
    }

    fun getPossibleRootFiles(project: Project): Set<VirtualFile> {
        if (DumbService.isDumb(project)) return emptySet()
        val rootFiles = mutableSetOf<VirtualFile>()

        val documentCommands = NewCommandsIndex.getByName("\\documentclass", project)
        documentCommands.filter { it.requiredParameterText(0) != null }
            .mapNotNullTo(rootFiles) { it.containingFile.virtualFile }

        project.getLatexRunConfigurations().forEach {
            it.mainFile?.let { mainFile -> rootFiles.add(mainFile) }
        }

        return rootFiles
    }

    private fun makePreparation(project: Project): BuildFilesetPreparation {
        // Get all bibtex input paths from the run configurations
        val bibInputPaths = project.getBibtexRunConfigurations().mapNotNull { config ->
            pathOrNull(config.environmentVariables.envs["BIBINPUTS"])
        }.toSet() + Path(".")
        val texInputPaths = getTexinputsPaths(project, emptySet()).mapNotNull(::pathOrNull).toSet() + Path(".")
        // Current directory is always a tex input path
        return BuildFilesetPreparation(
            project, texInputPaths, bibInputPaths
        )
    }

    private fun buildFilesetsNoCache(project: Project): LatexProjectFilesets {
        val sp = makePreparation(project)
        val roots = getPossibleRootFiles(project)
        val allFilesets = mutableSetOf<Fileset>()
        val fileSetMapping = mutableMapOf<VirtualFile, MutableSet<Fileset>>()
        for (root in roots) {
            val files = mutableSetOf(root)
            val fileSet = Fileset(files, root)
            val pending = mutableListOf(root)
            while (pending.isNotEmpty()) {
                val f = pending.removeLast()
                fileSetMapping.computeIfAbsent(f) { mutableSetOf() }.add(fileSet)
                val referred = getDirectReferredFiles(project, f, root, sp) // indeed, we may deal with the same file multiple times with different roots
                referred.forEach {
                    if (files.add(it)) pending.add(it) // new element added
                }
            }
            allFilesets.add(fileSet)
        }
        val mapping = fileSetMapping.mapValues { (file, filesets) ->
            val allFiles = filesets.flatMapTo(mutableSetOf(file)) { it.files }
            val scope = GlobalSearchScope.filesWithLibrariesScope(project, allFiles)
            FilesetData(filesets, allFiles, scope)
        }
        return LatexProjectFilesets(allFilesets, mapping)
    }

    /**
     * Builds the filesets for the given project, caching the result.
     */
    fun buildFilesets(project: Project, forceRefresh: Boolean = false): LatexProjectFilesets {
        val expirationInMs = if (forceRefresh) 0L else CACHE_EXPIRATION_IN_MS
        return TexifyProjectCacheService.getOrCompute(project, expirationInMs, ::buildFilesetsNoCache)
    }

    /**
     * Gets the filesets containing the given PsiFile.
     *
     */
    fun getFilesetsFor(psiFile: PsiFile): Set<Fileset> {
        val virtualFile = psiFile.virtualFile ?: return emptySet()
        val project = psiFile.project
        return buildFilesets(project).getData(project, virtualFile)?.filesets ?: emptySet()
    }

    /**
     * Gets the search scope containing all the filesets that contain the given PsiFile.
     */
    fun getFilesetScopeFor(file: PsiFile, project: Project = file.project): GlobalSearchScope {
        val virtualFile = file.virtualFile ?: return GlobalSearchScope.fileScope(file)
        val data = buildFilesets(project).getData(project, virtualFile)
        return data?.filesetScope ?: GlobalSearchScope.fileScope(file)
    }

    /**
     * Gets the related files for the given PsiFile, namely all files that are part of the same fileset as the given file.
     */
    fun getRelatedFilesFor(file: PsiFile): Set<VirtualFile> {
        val project = file.project
        val virtualFile = file.virtualFile ?: return emptySet()
        return buildFilesets(project).getData(project, virtualFile)?.relatedFiles ?: setOf(virtualFile)
    }

    fun getIncludedPackages(file: PsiFile): Set<String> {
        val project = file.project
        return getIncludedPackages(project, getFilesetScopeFor(file, project))
    }

    fun getIncludedPackages(project: Project, scope: GlobalSearchScope): Set<String> {
        return NewSpecialCommandsIndex.getAllPackageIncludes(project, scope).flatMap {
            it.collectSubtreeTyped<LatexParameterText>().map { it.text }
        }.toSet()
    }
}