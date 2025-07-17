package nl.hannahsten.texifyidea.index

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.findFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider.getGraphicsPaths
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.SUBFILES
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredFileArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.CacheValueTimed
import nl.hannahsten.texifyidea.util.ProjectCacheService
import nl.hannahsten.texifyidea.util.TexifyProjectCacheService
import nl.hannahsten.texifyidea.util.expandCommandsOnce
import nl.hannahsten.texifyidea.util.files.LatexPackageLocation
import nl.hannahsten.texifyidea.util.files.allChildDirectories
import nl.hannahsten.texifyidea.util.getBibtexRunConfigurations
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.getTexinputsPaths
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.projectSearchScope
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
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
    val countOfBuilding = AtomicInteger(0)
    val totalBuildTime = AtomicLong(0)

    const val DEFAULT_CACHE_EXPIRATION_IN_MS = 2_000L

    private val expirationTimeInMs: Long
        get() = TexifySettings.getInstance().filesetExpirationTimeMs

    private object UserDataKeys {
        /**
         * Stores the files that are referenced by the latex command.
         */
        val FILE_REFERENCE = Key.create<
            CacheValueTimed<
                Pair<List<String>, List<Set<VirtualFile>>> // List of pairs of original text and set of files in order
                >
            >("latex.command.reference.files")
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
        FilenameIndex.getVirtualFilesByName("main.tex", true, project.projectSearchScope)
            .filter { it.isValid }
            .forEach { rootFiles.add(it) }

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
        val files: MutableSet<VirtualFile> = mutableSetOf(root),
        var declareGraphicsExtensions: Set<String>? = null,
        var graphicsSuffix: Set<Path> = emptySet(),
        var luatexPaths: Set<VirtualFile> = emptySet(),
    ) : ProjectInfo(project, rootDirs, bibInputPaths, timestamp)

    private fun Path.findVirtualFile(): VirtualFile? = LocalFileSystem.getInstance().findFileByNioFile(this)

    private fun processElementsWithPaths0(
        elements: List<Sequence<Path>>,
        refInfoMap: List<MutableSet<VirtualFile>>,
        info: FilesetInfo,
        nextFile: (VirtualFile) -> Unit,
        action: (Path, (VirtualFile) -> Unit) -> Unit
    ) {
        elements.forEachIndexed { i, paths ->
            val newNextFile: (VirtualFile) -> Unit = { file ->
                refInfoMap[i].add(file)
                nextFile(file)
            }
            paths.forEach { path ->
                action(path, newNextFile)
            }
        }
    }

    private inline fun processElementsWithPaths(
        elements: List<Sequence<Path>>,
        refInfoMap: List<MutableSet<VirtualFile>>,
        info: FilesetInfo,
        noinline nextFile: (VirtualFile) -> Unit,
        crossinline action: (Path) -> VirtualFile?
    ) {
        processElementsWithPaths0(elements, refInfoMap, info, nextFile) { path, func ->
            action(path)?.apply(func)
        }
    }

    private fun processFilesUnderRootDirs(
        elements: List<Sequence<Path>>,
        refInfoMap: List<MutableSet<VirtualFile>>,
        info: FilesetInfo, rootDirs: Collection<VirtualFile>,
        nextFile: (VirtualFile) -> Unit
    ) {
        processElementsWithPaths0(elements, refInfoMap, info, nextFile) { path, func ->
            if (path.isAbsolute) path.findVirtualFile()?.apply(func)
            else rootDirs.forEach { sourcePath ->
                sourcePath.findFileByRelativePath(path.invariantSeparatorsPathString)?.apply(func)
            }
        }
    }

    private fun splitTextAndBuildRanges(text: String, delim: String, startOffset0: Int = 0): List<Pair<TextRange, String>> {
        val splits = text.split(delim)
        val result = ArrayList<Pair<TextRange, String>>(splits.size)
        var startOffset = startOffset0
        for (s in splits) {
            val range = TextRange(startOffset, startOffset + s.length)
            result.add(range to s)
            startOffset += s.length + delim.length // move the start offset to the next split
        }
        return result
    }

    private fun pathTextExtraProcessing(
        text: String, command: LatexCommands, file: VirtualFile, info: FilesetInfo
    ): String {
        var result = expandCommandsOnce(text, info.project, file)
        result = result.trim()
        return result
    }

    private fun findReferredFiles(
        command: LatexCommands, file: VirtualFile,
        info: FilesetInfo,
        nextFile: (VirtualFile) -> Unit
    ) {
        // remark: we should only use stub-based information here for performance reasons
        val commandName = command.name ?: return
        val reqParamTexts = command.requiredParametersText()

        val cmd = LatexCommand.lookup(commandName)?.firstOrNull() ?: return
        val dependency = cmd.dependency

        // If the command is a graphics command, we can use the declared graphics extensions
        val configuredExtensions = if (dependency == LatexPackage.GRAPHICX) info.declareGraphicsExtensions else null

        // let us locate the sub
        val rangesAndTextsWithExt: MutableList<Pair<List<String>, Set<String>>> = mutableListOf()
        // Special case for the subfiles package: the (only) mandatory optional parameter should be a path to the main file
        // We reference it because we include the preamble of that file, so it is in the file set (partially)
        if (commandName == LatexGenericRegularCommand.DOCUMENTCLASS.cmd && reqParamTexts.any { it.endsWith(SUBFILES.name) }) {
            // try to find the main file in the optional parameter map
            command.optionalParameterTextMap().entries.firstOrNull()?.let { (k, _) ->
                // the value should be empty, we only care about the key, see Latex.bnf
                rangesAndTextsWithExt.add(
                    listOf(k) to setOf("tex")
                )
            }
        }
        cmd.requiredArguments.zip(reqParamTexts).mapNotNullTo(rangesAndTextsWithExt) { (argument, contentText) ->
            if (argument !is RequiredFileArgument) return@mapNotNullTo null
            val paramTexts = if (argument.commaSeparatesArguments) {
                contentText.split(PatternMagic.parameterSplit)
            }
            else listOf(contentText)

            val extensions = configuredExtensions ?: argument.supportedExtensions
            paramTexts to extensions
        }

        val extractedRefTexts = rangesAndTextsWithExt.flatMap { it.first }

        var pathWithExts = rangesAndTextsWithExt.flatMap { (paramTexts, extensions) ->
            val noExtensionProvided = extensions.isEmpty()
            val extensionSeq = extensions.asSequence()
            paramTexts.asSequence().map { text ->
                val text = pathTextExtraProcessing(text, command, file, info)
                pathOrNull(text)?.let { path ->
                    if (path.extension.isNotEmpty() || noExtensionProvided) {
                        sequenceOf(path)
                    }
                    else {
                        path.fileName?.pathString?.let { fileName ->
                            extensionSeq.map { ext -> path.resolveSibling("$fileName.$ext") }
                        }
                    }
                } ?: emptySequence()
            }
        }

        var searchDirs = info.rootDirs

        if (commandName == LatexGenericRegularCommand.TIKZFIG.commandWithSlash || commandName == LatexGenericRegularCommand.CTIKZFIG.commandWithSlash) {
            searchDirs = searchDirs + searchDirs.mapNotNull { it.findDirectory("figures") }
        }

        if (dependency in CommandMagic.graphicPackages && info.graphicsSuffix.isNotEmpty()) {
            val graphicsSuffix = info.graphicsSuffix.asSequence()
            pathWithExts = pathWithExts.map { paths ->
                val allPathWithSuffix = paths.flatMap { path -> graphicsSuffix.map { suffix -> suffix.resolve(path) } }
                (paths + allPathWithSuffix)
            }
        }

        val refInfoMap: List<MutableSet<VirtualFile>> = List(extractedRefTexts.size) { mutableSetOf() }

        if (commandName in CommandMagic.packageInclusionCommands) {
            processElementsWithPaths(pathWithExts, refInfoMap, info, nextFile) {
                LatexPackageLocation.getPackageLocation(it.pathString, info.project)?.findVirtualFile()
            }
        }

        if (commandName in CommandMagic.bibliographyIncludeCommands) {
            // For bibliography files, we can search in the bib input paths
            processFilesUnderRootDirs(pathWithExts, refInfoMap, info, info.bibInputPaths, nextFile)
        }

        if (commandName == LatexGenericRegularCommand.EXTERNALDOCUMENT.commandWithSlash) {
            // \externaldocument uses the .aux file in the output directory, we are only interested in the source file,
            // but it can be anywhere (because no relative path will be given, as in the output directory everything will be on the same level).
            // This does not count for building the file set, because the external document is not actually in the fileset, only the label definitions are,
            // but we still include the files anyway, so that the user can navigate to them.
            // try to find everywhere in the project
            processElementsWithPaths0(pathWithExts, refInfoMap, info, nextFile) { path, function ->
                FilenameIndex.processFilesByName(path.fileName.pathString, true, info.project.projectSearchScope) {
                    if (it.isValid) function(it)
                    true
                }
            }
        }
        processFilesUnderRootDirs(pathWithExts, refInfoMap, info, searchDirs, nextFile)

        val savedData = extractedRefTexts to refInfoMap

        updateOrMergeRefData(command, savedData, info)
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
            val viaUsepackage = NewSpecialCommandsIndex.getPackageIncludes(project, file)
                .filter { it.requiredParameterText(0) == LatexPackage.ADDTOLUATEXPATH.name }
                .flatMap { it.optionalParameterTextMap().keys }
                .flatMap { it.split(",") }
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

    private fun updateOrMergeRefData(command: PsiElement, refInfoMap: Pair<List<String>, List<MutableSet<VirtualFile>>>, info: FilesetInfo) {
        val existingRef = command.getUserData(UserDataKeys.FILE_REFERENCE)
        if (existingRef == null || existingRef.timestamp < info.timestamp) {
            // overwrite the existing reference
            command.putUserData(UserDataKeys.FILE_REFERENCE, CacheValueTimed(refInfoMap, info.timestamp))
        }
        else if (existingRef.timestamp == info.timestamp) {
            // If the marker is the same, update the files
            val existingFiles = existingRef.value.second
            val (names, newFiles) = refInfoMap
            val updatedFiles = if (newFiles.size != existingFiles.size) {
                // this should not happen as the parsing process is the same, but just in case
                refInfoMap
            }
            else {
                names to List(newFiles.size) { index ->
                    newFiles[index] + existingFiles[index]
                }
            }
            command.putUserData(UserDataKeys.FILE_REFERENCE, CacheValueTimed(updatedFiles, info.timestamp))
        }
        // remark: as it is guaranteed by the cache service that the fileset update will not run in parallel with itself,
        // we can safely update the user data without interference
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
        val fileInputCommands = NewSpecialCommandsIndex.getAllFileInputs(project, file)

        processNewInformation(project, file, info)

        fileInputCommands.forEach {
            findReferredFiles(it, file, info, nextFile)
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

    private fun recursiveBuildFileset(file: VirtualFile, fileSetInfo: FilesetInfo) {
        // indeed, we may deal with the same file multiple times with different roots
        getDirectReferredFiles(file, fileSetInfo) {
            if (fileSetInfo.files.add(it)) {
                // new element added, continue building the fileset
                recursiveBuildFileset(it, fileSetInfo)
            }
        }
    }

    private fun buildFilesetFromRoot(
        root: VirtualFile, project: Project, projectInfo: ProjectInfo
    ): FilesetInfo {
        val fileSetInfo = makePreparation(project, root, projectInfo)
        recursiveBuildFileset(root, fileSetInfo)
        return fileSetInfo
    }

    private fun mergeFilesIntoFileset(files: Set<VirtualFile>, fs: FilesetInfo) {
        for (file in files) {
            if (fs.files.add(file)) {
                recursiveBuildFileset(file, fs)
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
                tomlFile.parent?.findFile("src/$it")
            }.toSet()
        }
        return filesets
    }

    fun buildFilesetsNow(project: Project): LatexProjectFilesets {
        countOfBuilding.incrementAndGet()
        val startTime = System.currentTimeMillis()
        val projectInfo = makePreparation(project)
        val roots = getPossibleRootFiles(project)
        val allFilesetInfo = mutableListOf<FilesetInfo>()
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
//      deal with the remaining files, or we can just ignore them?
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
        val allFilesets = allFilesetInfo.map { Fileset(it.files, it.root) }.toSet()
        val mapping: Map<VirtualFile, MutableSet<Fileset>> = buildMap {
            allFilesets.forEach {
                it.files.forEach { file ->
                    getOrPut(file) { mutableSetOf() }.add(it)
                }
            }
        }

        val dataMapping = mapping.mapValues { (file, filesets) ->
            val allFiles = filesets.flatMapTo(mutableSetOf(file)) { it.files }
            val scope = GlobalSearchScope.filesWithLibrariesScope(project, allFiles)
            FilesetData(filesets, allFiles, scope)
        }

        val elapsedTime = System.currentTimeMillis() - startTime
        totalBuildTime.addAndGet(elapsedTime)
        return LatexProjectFilesets(allFilesets, dataMapping)
    }

    private val CACHE_KEY = ProjectCacheService.createKey<LatexProjectFilesets>()

    private suspend fun buildFilesetsSuspend(project: Project): LatexProjectFilesets? {
        return withBackgroundProgress(project, "Building filesets") {
            smartReadAction(project) {
                buildFilesetsNow(project)
            }.also {
                // refresh the inspections
                if (!ApplicationManager.getApplication().isUnitTestMode) {
                    DaemonCodeAnalyzer.getInstance(project).restart()
                }
                // there will be an exception if we try to restart the daemon in unit tests
                // see FileStatusMap.CHANGES_NOT_ALLOWED_DURING_HIGHLIGHTING
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
        val expirationInMs = if (callRefresh) 0L else expirationTimeInMs
        return TexifyProjectCacheService.getInstance(project).getAndComputeLater(CACHE_KEY, expirationInMs, ::buildFilesetsSuspend)
    }

    /**
     * Checks if the filesets are available for the given project, and potentially schedules a recomputation.
     */
    fun isProjectFilesetsAvailable(project: Project): Boolean {
        return getFilesets(project) != null
//        return TexifyProjectCacheService.getInstance(project).getOrNull(CACHE_KEY) != null
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
    fun getFilesetScopeFor(file: PsiFile): GlobalSearchScope {
        val virtualFile = file.virtualFile ?: return GlobalSearchScope.fileScope(file)
        val project = file.project
        return getFilesetScopeFor(virtualFile, project)
    }

    fun getFilesetScopeFor(file: VirtualFile, project: Project): GlobalSearchScope {
        return getFilesets(project)?.getData(file)?.filesetScope ?: GlobalSearchScope.fileScope(project, file)
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
    fun commandFileReferenceInfo(command: LatexCommands, project: Project = command.project): Pair<List<String>, List<Set<VirtualFile>>>? {
        if (DumbService.isDumb(project)) return null

        val data = command.getUserData(UserDataKeys.FILE_REFERENCE)
        if (data != null && data.isNotExpired(expirationTimeInMs)) {
            // If the data is already computed and not expired, return it
            return data.value
        }
        val projectFS = getFilesets(project) // call for an update if needed
        val root = command.containingFile.virtualFile ?: return null
        if (projectFS == null || root in projectFS.mapping) {
            // if the file is already in the mapping, we should totally rely on the computed data
            return data?.value
        }

        // If the file is not in the mapping, it means it is not part of any fileset, so we manually build it as a root file
        val projectInfo = makePreparation(project)
        buildFilesetFromRoot(root, project, projectInfo)

        return command.getUserData(UserDataKeys.FILE_REFERENCE)?.value
    }
}