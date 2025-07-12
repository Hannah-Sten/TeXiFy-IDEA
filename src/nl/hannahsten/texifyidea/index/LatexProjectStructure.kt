package nl.hannahsten.texifyidea.index

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.hints.ParameterHintsPass
import com.intellij.codeInsight.hints.ParameterHintsPassFactory
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider.getGraphicsPaths
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.SUBFILES
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexOptionalParam
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.CacheValueTimed
import nl.hannahsten.texifyidea.util.ProjectCacheService
import nl.hannahsten.texifyidea.util.TexifyProjectCacheService
import nl.hannahsten.texifyidea.util.files.LatexPackageLocation
import nl.hannahsten.texifyidea.util.files.allChildDirectories
import nl.hannahsten.texifyidea.util.getBibtexRunConfigurations
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.getTexinputsPaths
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.projectSearchScope
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.contains
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

/**
 * Descriptions of filesets
 */
data class FilesetData(
    val filesets: Set<Fileset>,
    val relatedFiles: Set<VirtualFile>,
    val filesetScope: GlobalSearchScope,
)

data class LatexProjectFilesets(
    val filesets: Set<Fileset>,
    val mapping: Map<VirtualFile, FilesetData>,
) {
    fun getData(file: VirtualFile): FilesetData? {
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
    /**
     * The count of building operations, used for debugging purposes.
     */
    private val countOfBuilding = AtomicInteger(0)

    object UserDataKeys {
        /**
         * Stores the files that are referenced by the latex command.
         */
        val FILE_REFERENCE = Key.create<CacheValueTimed<Set<VirtualFile>>>("latex.command.reference.files")
    }

    const val CACHE_EXPIRATION_IN_MS = 5_000L // 5 seconds

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

    private open class ProjectInfo(
        val project: Project,
        val rootDirs: Set<VirtualFile>,
        val bibInputPaths: Set<VirtualFile>,
        val timestamp: Long // a marker to identify the current build process
    )

    /**
     * Temporary information for building a fileset.
     */
    private class FilesetInfo(
        project: Project,
        rootDirs: MutableSet<VirtualFile>, bibInputPaths: MutableSet<VirtualFile>,
        timestamp: Long,
        val root: VirtualFile,
        var declareGraphicsExtensions: Set<String>? = null,
        var graphicsSuffix: Set<Path> = emptySet(),
        var luatexPaths: Set<VirtualFile> = emptySet(),
    ) : ProjectInfo(project, rootDirs, bibInputPaths, timestamp)

    private fun Path.findVirtualFile(): VirtualFile? = LocalFileSystem.getInstance().findFileByNioFile(this)

    private fun processFilesUnderRootDirs(path: Path, rootDirs: Collection<VirtualFile>, nextFile: (VirtualFile) -> Unit) {
        if (path.isAbsolute) {
            path.findVirtualFile()?.let { nextFile(it) }
            return
        }
        for (sourcePath in rootDirs) {
            sourcePath.findFileByRelativePath(path.invariantSeparatorsPathString)?.let { file ->
                nextFile(file)
            }
        }
    }

    private fun findReferredFiles(
        command: LatexCommands, file: VirtualFile,
        info: FilesetInfo,
        nextFile: (VirtualFile) -> Unit
    ) {
        val commandName = command.name ?: return
        val reqParamTexts = command.requiredParametersText()


        val cmd = LatexCommand.lookup(commandName)?.firstOrNull() ?: return
        val dependency = cmd.dependency

        // If the command is a graphics command, we can use the declared graphics extensions
        val configuredExtensions = if (dependency == LatexPackage.GRAPHICX) info.declareGraphicsExtensions else null

        // let us locate the sub
        val pathTextAndExt = cmd.requiredArguments.zip(reqParamTexts).mapNotNullTo(mutableListOf()) { (argument, paramText) ->
            if (argument !is RequiredFileArgument) return@mapNotNullTo null
            val pathTexts = if (argument.commaSeparatesArguments) paramText.split(",") else listOf(paramText)
            val extensions = configuredExtensions ?: argument.supportedExtensions
            pathTexts to extensions
        }
        // Special case for the subfiles package: the (only) mandatory optional parameter should be a path to the main file
        // We reference it because we include the preamble of that file, so it is in the file set (partially)
        if (commandName == LatexGenericRegularCommand.DOCUMENTCLASS.cmd && reqParamTexts.any { it.endsWith(SUBFILES.name) }) {
            // try to find the main file in the optional parameter map
            command.findFirstChildTyped<LatexOptionalParam>()?.text?.trim('[', ']')?.let {
                pathTextAndExt.add(
                    listOf(it) to setOf("tex")
                )
            }
        }

        // TODO file name not null

        val pathAndExts = pathTextAndExt.flatMap { (pathTexts, extensions) ->
            pathTexts.asSequence().mapNotNull { pathOrNull(it.trim()) }.flatMap {
                if (it.extension.isNotEmpty()) sequenceOf(it)
                else extensions.asSequence().map { ext -> it.resolveSibling("${it.fileName.pathString}.$ext") }
            }
        }

        var searchDirs = info.rootDirs

        if (commandName == LatexGenericRegularCommand.TIKZFIG.commandWithSlash || commandName == LatexGenericRegularCommand.CTIKZFIG.commandWithSlash) {
            searchDirs = searchDirs + searchDirs.mapNotNull { it.findDirectory("figures") }
        }

        if (commandName in CommandMagic.packageInclusionCommands) {
            pathAndExts.forEach { it ->
                LatexPackageLocation.getPackageLocation(it.pathString, info.project)?.findVirtualFile()?.let {
                    nextFile(it)
                }
            }
        }

        if (commandName in CommandMagic.bibliographyIncludeCommands) {
            // For bibliography files, we can search in the bib input paths
            pathAndExts.forEach { path ->
                processFilesUnderRootDirs(path, info.bibInputPaths, nextFile)
            }
        }

        if (dependency in CommandMagic.graphicBackages) {
            // For graphics paths, we can search in the graphics paths
            info.graphicsSuffix.forEach { suffix ->
                pathAndExts.forEach {
                    processFilesUnderRootDirs(suffix.resolve(it), searchDirs, nextFile)
                }
            }
        }



        if (commandName == LatexGenericRegularCommand.EXTERNALDOCUMENT.commandWithSlash) {
            // \externaldocument uses the .aux file in the output directory, we are only interested in the source file,
            // but it can be anywhere (because no relative path will be given, as in the output directory everything will be on the same level).
            // This does not count for building the file set, because the external document is not actually in the fileset, only the label definitions are,
            // but we still include the files anyway, so that the user can navigate to them.
            // try to find everywhere in the project
            pathAndExts.forEach {
                FilenameIndex.getVirtualFilesByName(it.fileName.pathString, info.project.projectSearchScope).forEach(nextFile)
            }
        }

        pathAndExts.forEach { path ->
            processFilesUnderRootDirs(path, searchDirs, nextFile)
        }
    }

    /**
     * Add new information such as declared graphics extensions and luatex paths to the given fileset info.
     */
    private fun processNewInformation(
        project: Project, file: VirtualFile, info: FilesetInfo
    ) {
        // Declare graphics extensions
        NewCommandsIndex.getByName(LatexGenericRegularCommand.DECLAREGRAPHICSEXTENSIONS.command, project, file)
            .lastOrNull()
            ?.requiredParameterText(0)
            ?.split(",")
            // Graphicx requires the dot to be included
            ?.map { it.trim(' ', '.') }?.toSet()?.let {
                info.declareGraphicsExtensions = it
            }

        NewCommandsIndex.getByNames(CommandMagic.graphicPathsCommandNames, project, file)
            .lastOrNull()
            ?.getGraphicsPaths()
            ?.mapNotNull { pathOrNull(it) }
            ?.toSet()
            ?.let {
                info.graphicsSuffix = it
            }

        run {
            // addtoluatexpath
            val direct = NewCommandsIndex.getByName(LatexGenericRegularCommand.ADDTOLUATEXPATH.cmd, project, file)
                .mapNotNull { it.requiredParameterText(0) }
                .flatMap { it.split(",") }
            val viaUsepackage = NewCommandsIndex.getByNames(CommandMagic.packageInclusionCommands, project, file)
                .filter { it.requiredParameterText(0) == LatexPackage.ADDTOLUATEXPATH.name }
                .flatMap { it.getOptionalParameterMap().keys }
                .flatMap { it.text.split(",") }
            val directories = (direct + viaUsepackage).flatMap {
                val basePath = LocalFileSystem.getInstance().findFileByPath(it.trimEnd('/', '*')) ?: return@flatMap emptyList()
                if (it.endsWith("/**")) {
                    basePath.allChildDirectories()
                }
                else if (it.endsWith("/*")) {
                    basePath.children.filter { child -> child.isDirectory }
                }
                else {
                    listOf(basePath)
                }
            }
            if (directories.isNotEmpty()) {
                info.luatexPaths = info.luatexPaths + directories
            }
        }
    }

    private fun updateOrMergeRefData(command: PsiElement, files: List<VirtualFile>, info: FilesetInfo) {
        val existingRef = command.getUserData(UserDataKeys.FILE_REFERENCE)
        if (existingRef != null && existingRef.timestamp == info.timestamp) {
            // If the marker is the same, update the files
            val existingFiles = existingRef.value
            val updatedFiles = existingFiles + files
            command.putUserData(UserDataKeys.FILE_REFERENCE, CacheValueTimed(updatedFiles, info.timestamp))
        }
        else {
            // If there is no existing reference, create a new one
            command.putUserData(UserDataKeys.FILE_REFERENCE, CacheValueTimed(files.toSet(), info.timestamp))
        }
        // remark: as it is guaranteed by the cache service that the fileset update will not run in parallel with itself, we can safely update the user data without interference
    }

    /**
     * Gets the directly referred files from the given file relative to the given root file.
     *
     * Note: the path of the referred file is relative to the root file, not the file that contains the input command.
     */
    private fun getDirectReferredFiles(
        file: VirtualFile, info: FilesetInfo, nextFile: (VirtualFile) -> Unit
    ) {
        if (!file.isValid) return
        val project = info.project
        val psiFile = file.findPsiFile(project) ?: return
        val fileInputCommands = CachedValuesManager.getCachedValue(psiFile) {
            Result.create(NewSpecialCommandsIndex.getAllFileInputs(project, file), file)
        }

        processNewInformation(project, file, info)

        fileInputCommands.forEach {
            findReferredFiles(it, file, info,nextFile)
        }
    }

    private fun makePreparation(project: Project): ProjectInfo {
        // Get all bibtex input paths from the run configurations
        val bibInputPaths = project.getBibtexRunConfigurations().mapNotNull { config ->
            (config.environmentVariables.envs["BIBINPUTS"])?.let {
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
            project, texInputPaths, bibInputPaths, System.currentTimeMillis() // marker to identify the current build process, can be anything that is unique for the current build
        )
    }

    private fun makePreparation(project: Project, root: VirtualFile, projectInfo: ProjectInfo): FilesetInfo {
        val texInputPaths = projectInfo.rootDirs.toMutableSet()
        val bibInputPaths = projectInfo.bibInputPaths.toMutableSet()
        root.parent?.let {
            texInputPaths.add(it)
            bibInputPaths.add(it)
        }
        return FilesetInfo(
            project, texInputPaths, bibInputPaths, projectInfo.timestamp, root
        )
    }


    private fun recursiveBuildFileset(
        file: VirtualFile, files: MutableSet<VirtualFile>,
        fileSetInfo: FilesetInfo
    ) {
        // indeed, we may deal with the same file multiple times with different roots
        getDirectReferredFiles(file, fileSetInfo) {
            if (files.add(it)) {
                // new element added, continue building the fileset
                recursiveBuildFileset(it, files, fileSetInfo)
            }
        }
    }

    private fun buildFilesetFromRoot(
        root: VirtualFile, project: Project, projectInfo: ProjectInfo
    ): Fileset {
        val files = mutableSetOf(root)
        val fileSetInfo = makePreparation(project, root, projectInfo)
        recursiveBuildFileset(root, files, fileSetInfo)
        return Fileset(files, root)
    }

    fun buildFilesetsNow(project: Project): LatexProjectFilesets {
        countOfBuilding.incrementAndGet()
        val projectInfo = makePreparation(project)
        val roots = getPossibleRootFiles(project)
        val allFilesets = mutableSetOf<Fileset>()
        val fileSetMapping = mutableMapOf<VirtualFile, MutableSet<Fileset>>()

        for (root in roots) {
            val fileset = buildFilesetFromRoot(root, project, projectInfo)
            allFilesets.add(fileset)
            for (file in fileset.files) {
                // add the file to the mapping
                fileSetMapping.computeIfAbsent(file) { mutableSetOf() }.add(fileset)
            }
        }
//        // deal with the remaining files
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        projectFileIndex.iterateContent { vf ->
            if (vf.fileType == LatexFileType && vf !in fileSetMapping) {
                val fileset = buildFilesetFromRoot(vf, project, projectInfo)
                allFilesets.add(fileset)
                for (file in fileset.files) {
                    // add the file to the mapping
                    fileSetMapping.computeIfAbsent(file) { mutableSetOf() }.add(fileset)
                }
            }
            true
        }

        val mapping = fileSetMapping.mapValues { (file, filesets) ->
            val allFiles = filesets.flatMapTo(mutableSetOf(file)) { it.files }
            val scope = GlobalSearchScope.filesWithLibrariesScope(project, allFiles)
            FilesetData(filesets, allFiles, scope)
        }
        return LatexProjectFilesets(allFilesets, mapping)
    }


    private val CACHE_KEY = ProjectCacheService.createKey<LatexProjectFilesets>()

    private suspend fun buildFilesetsSuspend(project: Project): LatexProjectFilesets? {
        return withBackgroundProgress(project, "Building filesets") {
            smartReadAction(project) {
                buildFilesetsNow(project)
            }.also {
                // refresh the inspections
                @Suppress("UnstableApiUsage")
                writeAction {
                    ParameterHintsPassFactory.forceHintsUpdateOnNextPass()
                    // refresh the inspections to update the filesets
                    DaemonCodeAnalyzer.getInstance(project).restart()
                }
            }
        }
    }

    suspend fun updateFilesetsNow(project: Project) {
        TexifyProjectCacheService.getInstance(project).computeAndUpdate(CACHE_KEY, ::buildFilesetsSuspend)
    }

    /**
     * Gets the recently built filesets for the given project and schedule a recomputation if they are not available or expired.
     */
    fun getFilesets(project: Project, callRefresh: Boolean = false): LatexProjectFilesets? {
        val expirationInMs = if (callRefresh) 0L else CACHE_EXPIRATION_IN_MS
        return TexifyProjectCacheService.getInstance(project).getAndComputeLater(CACHE_KEY, expirationInMs, ::buildFilesetsSuspend)
    }

    /**
     * Gets the filesets containing the given PsiFile.
     *
     */
    fun getFilesetsFor(psiFile: PsiFile): Set<Fileset> {
        val virtualFile = psiFile.virtualFile ?: return emptySet()
        val project = psiFile.project
        return getFilesets(project)?.getData(virtualFile)?.filesets ?: emptySet()
    }

    /**
     * Gets the search scope containing all the filesets that contain the given PsiFile.
     */
    fun getFilesetScopeFor(file: PsiFile, project: Project = file.project): GlobalSearchScope {
        val virtualFile = file.virtualFile ?: return GlobalSearchScope.fileScope(file)
        val data = getFilesets(project)?.getData(virtualFile)
        return data?.filesetScope ?: GlobalSearchScope.fileScope(file)
    }

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

    fun getIncludedPackages(file: PsiFile): Set<String> {
        val project = file.project
        return getIncludedPackages(project, getFilesetScopeFor(file, project))
    }

    fun getIncludedPackages(project: Project, scope: GlobalSearchScope): Set<String> {
        return NewSpecialCommandsIndex.getAllPackageIncludes(project, scope).flatMap {
            it.collectSubtreeTyped<LatexParameterText>().map { it.text }
        }.toSet()
    }

    /**
     *
     */
    fun commandReferringFiles(command: LatexCommands): Set<VirtualFile> {
        val project = command.project
        if (DumbService.isDumb(project)) return emptySet()
        val projectFS = getFilesets(project) // call for an update if needed
        command.getUserData(UserDataKeys.FILE_REFERENCE)?.let { return it.value } // if it is already available, return it

        val root = command.containingFile.virtualFile ?: return emptySet()
        if (projectFS == null || root in projectFS.mapping) {
            return emptySet()
        }
        // If the file is not in the mapping, it means it is not part of any fileset, so we manually build it as a root file
        val projectInfo = makePreparation(project)
        buildFilesetFromRoot(root, project, projectInfo)

        return command.getUserData(UserDataKeys.FILE_REFERENCE)?.value ?: emptySet()
    }
}