package nl.hannahsten.texifyidea.index.projectstructure

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFileOrDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.*
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Provides methods to build and manage the fileset structure for LaTeX files.
 *
 * @author Ezrnest
 */
object LatexProjectStructure {

    val performanceTracker = SimplePerformanceTracker()

    val expirationTime: Duration
        get() = TexifySettings.getState().filesetExpirationTimeMs.milliseconds

    /**
     * Stores the files that are referenced by the latex command.
     */
    // List of pairs of original text and set of files in order
    val userDataKeyFileReference = Key.create<
        CacheValueTimed<
            Pair<List<String>, List<Set<VirtualFile>>>
            >
        >("latex.command.reference.files")

    fun getPossibleRootFiles(project: Project): Set<VirtualFile> {
        if (DumbService.isDumb(project)) return emptySet()
        val rootFiles = mutableSetOf<VirtualFile>()

        val documentCommands = NewCommandsIndex.getByName("\\documentclass", project)
        documentCommands.filter { it.requiredParameterText(0) != null }
            .mapNotNullTo(rootFiles) { it.containingFile.virtualFile }

        project.getLatexRunConfigurations().forEach {
            LatexRunConfigurationStaticSupport.resolveMainFile(it)?.let { mainFile -> rootFiles.add(mainFile) }
        }
        FilenameIndex.getVirtualFilesByName("main.tex", true, project.contentSearchScope)
            .filter { it.isValid }
            .forEach { rootFiles.add(it) }

        return rootFiles
    }

    open class ProjectInfo(
        val project: Project,
        var rootDirs: Set<VirtualFile>,
        val bibInputPaths: Set<VirtualFile>,
        val timestamp: Instant = Clock.System.now()
    )

    fun Path.findVirtualFile(): VirtualFile? = LocalFileSystem.getInstance().findFileByNioFile(this)

    private fun makePreparation(project: Project): ProjectInfo {
        // Get all bibtex input paths from the run configurations
        val bibInputPaths = project.getLatexRunConfigurations().mapNotNull { config ->
            config.environmentVariables.envs["BIBINPUTS"]?.let {
                LocalFileSystem.getInstance().findFileByPath(it)
            }
        }.toMutableSet()
        val texInputPaths = getTexinputsPaths(project, emptySet()).mapNotNull { LocalFileSystem.getInstance().findFileByPath(it) }.toMutableSet()

        /*
        // "Try content roots, also for non-MiKTeX situations to allow using this as a workaround in case references can't be resolved the regular way"
        // But it may resolve to some file while latex cannot find it, leading to hidden warnings.

        val contentSourceRoots = ProjectRootManager.getInstance(project).contentSourceRoots
        texInputPaths += contentSourceRoots
        bibInputPaths += contentSourceRoots
         */

        return ProjectInfo(
            project, texInputPaths, bibInputPaths
        )
    }

    private fun makePreparation(project: Project, root: VirtualFile, projectInfo: ProjectInfo): FilesetProcessor {
        val texInputPaths = projectInfo.rootDirs.toMutableSet()
        val bibInputPaths = projectInfo.bibInputPaths.toMutableSet()
        root.parent?.let {
            texInputPaths.add(it)
            bibInputPaths.add(it)
        }
        return FilesetProcessor(
            project, texInputPaths, bibInputPaths, projectInfo.timestamp, root
        )
    }

    private fun buildFilesetFromRoot(
        root: VirtualFile, project: Project, projectInfo: ProjectInfo
    ): FilesetProcessor {
        val fileSetInfo = makePreparation(project, root, projectInfo)
        fileSetInfo.recursiveBuildFileset(root)
        return fileSetInfo
    }

    private fun mergeFilesIntoFileset(files: Set<VirtualFile>, fs: FilesetProcessor) {
        for (file in files) {
            if (fs.files.add(file)) {
                fs.recursiveBuildFileset(file)
            }
        }
    }

    /**
     * Check for tectonic.toml files in the project.
     * These files can input multiple tex files, which would then be in the same file set.
     * Example file: https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/3773#issuecomment-2503221732
     * @return List of sets of files included by the same toml file.
     */
    private fun findTectonicTomlInclusions(project: Project): List<Set<VirtualFile>> {
        // Actually, according to https://tectonic-typesetting.github.io/book/latest/v2cli/build.html?highlight=tectonic.toml#remarks Tectonic.toml files can appear in any parent directory, but we only search in the project for now
        val tomlFiles = FilenameIndex.getVirtualFilesByName("Tectonic.toml", true, project.projectSearchScope)
        val filesets = tomlFiles.mapNotNull { tomlFile ->
            val data = TomlMapper().readValue(File(tomlFile.path), Map::class.java)
            val output = (data.getOrDefault("output", null) as? List<*> ?: return@mapNotNull null).firstOrNull() as? Map<*, *>
            // The Inputs field was added after 0.15.0, at the moment of writing unreleased so we cannot check the version
            val inputs = if (output?.keys?.contains("inputs") == true) {
                val inputListMaybe = output.getOrDefault("inputs", listOf("_preamble.tex", "index.tex", "_postamble.tex"))
                if (inputListMaybe is String) listOf(inputListMaybe) else inputListMaybe as? List<*> ?: return@mapNotNull null
            }
            else {
                // See https://tectonic-typesetting.github.io/book/latest/ref/tectonic-toml.html#contents
                val preamble = output?.getOrDefault("preamble", "_preamble.tex") as? String ?: return@mapNotNull null
                val index = output.getOrDefault("index", "index.tex") as? String ?: return@mapNotNull null
                val postamble = output.getOrDefault("postamble", "_postamble.tex") as? String ?: return@mapNotNull null
                listOf(preamble, index, postamble)
            }
            // Inputs can be either a map "inline" -> String or file name
            // Actually it can also be just a single file name, but then we don't need all this gymnastics
            inputs.filterIsInstance<String>().mapNotNull {
                tomlFile.parent?.findFileOrDirectory("src/$it")
            }.toSet()
        }
        return filesets
    }

    private fun buildFilesets(project: Project): LatexProjectFilesets {
        val projectInfo = makePreparation(project)
        val roots = getPossibleRootFiles(project)
        val allFilesetInfo = mutableListOf<FilesetProcessor>()
        val processedFiles = mutableSetOf<VirtualFile>()

        for (root in roots) {
            val fileset = buildFilesetFromRoot(root, project, projectInfo)
            allFilesetInfo.add(fileset)
            processedFiles.addAll(fileset.files)
        }
        // let us try to merge the filesets that are created by Tectonic.toml files
        val tectonicFilesets = findTectonicTomlInclusions(project)
        for (files in tectonicFilesets) {
            if (files.isEmpty()) continue
            for (fs in allFilesetInfo) {
                if (fs.root in files) {
                    // this fileset already contains the root file, we can merge the files
                    mergeFilesIntoFileset(files, fs)
                }
            }
        }
        // deal with the remaining files, or we can just ignore them?
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        projectFileIndex.iterateContent { vf ->
            if (vf.fileType == LatexFileType && vf !in processedFiles) {
                // this is a standalone file, not included in any fileset yet
                val fileset = buildFilesetFromRoot(vf, project, projectInfo)
                allFilesetInfo.add(fileset)
                processedFiles.addAll(fileset.files)
            }
            true
        }

        // final processing
        val allFilesets = allFilesetInfo.map { it.createFileset() }.toSet()
        val mapping: Map<VirtualFile, MutableSet<Fileset>> = buildMap {
            allFilesets.forEach {
                it.files.forEach { file ->
                    getOrPut(file) { mutableSetOf() }.add(it)
                }
            }
        }

        val dataMapping = mapping.mapValues { (_, filesets) ->
            val allFiles = filesets.unionBy { it.files }
            val allLibs = filesets.unionBy { it.libraries }
            val scope = GlobalSearchScope.union(filesets.map { it.allFileScope })
            val externalDocumentInfo = if (filesets.size == 1) {
                filesets.first().externalDocumentInfo
            }
            else {
                filesets.flatMap { it.externalDocumentInfo }
            }

            FilesetData(filesets, allFiles, scope, allLibs, externalDocumentInfo)
        }

        return LatexProjectFilesets(allFilesets, dataMapping)
    }

    private val CACHE_KEY = GenericCacheService.createKey<LatexProjectFilesets>()

    private fun fallbackFilesets(previous: LatexProjectFilesets?): LatexProjectFilesets = previous ?: LatexProjectFilesets(emptySet(), emptyMap())

    private suspend fun buildFilesetsSuspend(project: Project, previous: LatexProjectFilesets?): LatexProjectFilesets {
        if (DumbService.isDumb(project)) return fallbackFilesets(previous)

        // Don't use smartReadAction here, as it may lead to a deadlock in tests
        val newFileset = try {
            readAction {
                if (DumbService.isDumb(project)) return@readAction null
                performanceTracker.track {
                    buildFilesets(project)
                }
            }
        }
        catch (_: IndexNotReadyException) {
            Log.debug("Skipping fileset rebuild because project index is not ready")
            null
        }

        val result = newFileset ?: fallbackFilesets(previous)
        if (!ApplicationManager.getApplication().isUnitTestMode && newFileset != null && newFileset != previous) {
            // refresh the inspections
            DaemonCodeAnalyzer.getInstance(project).restart()
            // there will be an exception if we try to restart the daemon in unit tests
            // see FileStatusMap.CHANGES_NOT_ALLOWED_DURING_HIGHLIGHTING
        }
        return result
    }

    /**
     * Calls to update the filesets for the given project.
     * This will ensure that the filesets are recomputed and up-to-date.
     */
    suspend fun updateFilesetsSuspend(project: Project): LatexProjectFilesets = TexifyProjectCacheService.getInstance(project).ensureRefresh(CACHE_KEY, ::buildFilesetsSuspend)

    /**
     * Gets the recently built filesets for the given project and schedule a recomputation if they are not available or expired.
     */
    fun getFilesets(project: Project, callRefresh: Boolean = false): LatexProjectFilesets? {
        val expiration = if (callRefresh) Duration.ZERO else expirationTime
        return TexifyProjectCacheService.getInstance(project).getAndComputeLater(CACHE_KEY, expiration, ::buildFilesetsSuspend)
    }

    /**
     * Checks if the filesets are available for the given project, and potentially schedules a recomputation.
     */
    fun isProjectFilesetsAvailable(project: Project): Boolean = getFilesets(project) != null

    fun getFilesetDataFor(virtualFile: VirtualFile, project: Project): FilesetData? = getFilesets(project)?.getData(virtualFile)

    fun getFilesetDataFor(psiFile: PsiFile): FilesetData? {
        val virtualFile = psiFile.virtualFile ?: return null
        val project = psiFile.project
        return getFilesetDataFor(virtualFile, project)
    }

    /**
     * Gets the filesets containing the given PsiFile.
     *
     */
    fun getFilesetsFor(psiFile: PsiFile): Set<Fileset> {
        val virtualFile = psiFile.virtualFile ?: return emptySet()
        val project = psiFile.project
        return getFilesetDataFor(virtualFile, project)?.filesets ?: emptySet()
    }

    /**
     * Gets the search scope containing all the filesets that contain the given PsiFile.
     *
     * @param onlyTexFiles if true, only `.tex` files in the project will be included. Sometimes we do not want to search in other files.
     */
    fun getFilesetScopeFor(file: PsiFile, onlyTexFiles: Boolean = false): GlobalSearchScope {
        val virtualFile = file.virtualFile ?: return GlobalSearchScope.fileScope(file)
        val project = file.project
        return getFilesetScopeFor(virtualFile, project, onlyTexFiles)
    }

    fun getFilesetScopeFor(file: VirtualFile, project: Project, onlyTexFiles: Boolean = false, onlyProjectFiles: Boolean = false): GlobalSearchScope {
        val data = getFilesets(project)?.getData(file) ?: return GlobalSearchScope.fileScope(project, file)
        var scope = data.filesetScope
        if (onlyTexFiles) {
            scope = scope.restrictedByFileTypes(LatexFileType)
        }
        if (onlyProjectFiles) {
            scope = scope.intersectWith(ProjectScope.getContentScope(project))
        }
        return scope
    }

    @Suppress("unused")
    fun getRootfilesFor(file: PsiFile): Set<VirtualFile> {
        val project = file.project
        val virtualFile = file.virtualFile ?: return emptySet()
        val data = getFilesets(project)?.getData(virtualFile) ?: return setOf(virtualFile) // If no related fileset is found, return the file itself as the only root file
        return data.filesets.mapTo(mutableSetOf()) { it.root }
    }

    /**
     * Gets the related files for the given PsiFile, namely all files that are part of the same fileset as the given file.
     */
    fun getRelatedFilesFor(file: PsiFile): Set<VirtualFile> {
        val project = file.project
        val virtualFile = file.virtualFile ?: return emptySet()
        return getFilesets(project)?.getData(virtualFile)?.relatedFiles ?: setOf(virtualFile)
    }

    /**
     * Gets the most recent file reference information for the given command, which is computed during the fileset building process.
     * Returns a map where the keys are plain texts ranges in the command that are references and the values are sets of virtual files that are referenced.
     *
     * Note that the returned data may not be valid.
     *
     * The reason that only plain texts are returned is that no further information can be obtained from stub-based commands.
     *
     *
     * @see nl.hannahsten.texifyidea.reference.InputFileReference
     */
    fun commandFileReferenceInfo(command: LatexCommands, requestRefresh: Boolean = false): Pair<List<String>, List<Set<VirtualFile>>>? {
        val data = command.getUserData(userDataKeyFileReference)
        if (!requestRefresh && !ApplicationManager.getApplication().isUnitTestMode) {
            return data?.value
        }
        if (data != null && data.isNotExpired(expirationTime)) {
            // If the data is already computed and not expired, return it
            return data.value
        }
        val containingFile = command.containingFile
        val project = containingFile.project
        val projectFS = getFilesets(project) // call for an update if needed
        val root = containingFile.virtualFile ?: return null
        if (projectFS == null || root in projectFS.mapping || !ProjectFileIndex.getInstance(project).isInProject(root)) {
            // if the file is already in the mapping, we should totally rely on the computed data
            return data?.value
        }
        if (DumbService.isDumb(project)) return null
        // If the file is not in the mapping, it means it is not part of any fileset, so we manually build it as a root file
        val projectInfo = makePreparation(project)
        buildFilesetFromRoot(root, project, projectInfo)
        return command.getUserData(userDataKeyFileReference)?.value
    }
}

fun pathOrNull(pathText: String?): Path? {
    if (pathText.isNullOrBlank()) return null
    return try {
        Path(pathText)
    }
    catch (_: InvalidPathException) {
        null
    }
}

fun GlobalSearchScope.restrictedByFileTypes(vararg fileTypes: FileType): GlobalSearchScope = GlobalSearchScope.getScopeRestrictedByFileTypes(this, *fileTypes)
