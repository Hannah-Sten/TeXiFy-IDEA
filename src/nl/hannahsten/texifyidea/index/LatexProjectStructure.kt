package nl.hannahsten.texifyidea.index

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.findFileOrDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider.getGraphicsPaths
import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.file.LatexSourceFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.index.file.LatexRegexBasedIndex
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.SUBFILES
import nl.hannahsten.texifyidea.lang.SimpleFileInputContext
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithSlash
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.AbstractBlockingCacheService
import nl.hannahsten.texifyidea.util.CacheValueTimed
import nl.hannahsten.texifyidea.util.GenericCacheService
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.TexifyProjectCacheService
import nl.hannahsten.texifyidea.util.contentSearchScope
import nl.hannahsten.texifyidea.util.expandCommandsOnce
import nl.hannahsten.texifyidea.util.files.LatexPackageLocation
import nl.hannahsten.texifyidea.util.files.allChildDirectories
import nl.hannahsten.texifyidea.util.getBibtexRunConfigurations
import nl.hannahsten.texifyidea.util.getLatexRunConfigurations
import nl.hannahsten.texifyidea.util.getTexinputsPaths
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.projectSearchScope
import nl.hannahsten.texifyidea.util.unionBy
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.SequencedSet
import kotlin.collections.contains
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// May be modified in the future
/**
 * A fileset is a set of files that are related to each other, e.g. a main file and its included files (including itself).
 * One file can belong to multiple filesets, e.g. if it is included in multiple main files.
 *
 * When resolving subfiles, we must use the relative path from the root file rather than the file that contains the input command.
 */
data class Fileset(
    val root: VirtualFile,
    /**
     * The files in the fileset
     */
    val files: SequencedSet<VirtualFile>,
    val libraries: Set<String>,
    val allFileScope: GlobalSearchScope,
    val externalDocumentInfo: List<ExternalDocumentInfo>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fileset

        if (root != other.root) return false
        if (files != other.files) return false

        return true
    }

    override fun hashCode(): Int {
        var result = root.hashCode()
        result = 31 * result + files.hashCode()
        return result
    }

    fun projectFileScope(project: Project): GlobalSearchScope {
        return allFileScope.intersectWith(project.contentSearchScope)
    }
}

data class ExternalDocumentInfo(
    val labelPrefix: String,
    val files: Set<VirtualFile>,
)

/**
 * Integrated descriptions of all filesets that are related to a specific file.
 */
data class FilesetData(
    val filesets: Set<Fileset>,
    /**
     * The union of all files in the related [filesets].
     */
    val relatedFiles: Set<VirtualFile>,
    /**
     * The scope that contains all files in [relatedFiles].
     */
    val filesetScope: GlobalSearchScope,

    val libraries: Set<String>,

    val externalDocumentInfo: List<ExternalDocumentInfo>
) {
    override fun equals(other: Any?): Boolean {
        // filesets uniquely determine the rest of the data
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilesetData

        return filesets == other.filesets
    }

    override fun hashCode(): Int {
        return filesets.hashCode()
    }
}

/**
 * Describes the filesets of a project, containing all filesets and a mapping from files to their fileset data.
 */
data class LatexProjectFilesets(
    val filesets: Set<Fileset>,
    val mapping: Map<VirtualFile, FilesetData>,
) {
    fun getData(file: VirtualFile): FilesetData? {
        return mapping[file]
    }

    override fun equals(other: Any?): Boolean {
        // we only need to compare the filesets, as they uniquely determine the mapping
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LatexProjectFilesets

        return filesets == other.filesets
    }

    override fun hashCode(): Int {
        return filesets.hashCode()
    }
}

class LatexLibraryInfo(
    val name: LatexLib,
    val location: VirtualFile,
    val files: Set<VirtualFile>,
    /**
     * The set of package names that are directly included in this package, without transitive dependencies and this package itself.
     */
    val directDependencies: Set<String>,
    /**
     * The set of all package names that are included in this package, including transitive dependencies and this package itself.
     */
    val allIncludedPackageNames: Set<String>,
) {

    val isPackage: Boolean
        get() = name.isPackageFile

    val isClass: Boolean
        get() = name.isClassFile

    override fun toString(): String {
        return "PackageInfo(name='$name', location=${location.path}, files=${files.size})"
    }
}

/**
 *
 * Provides methods to build and manage the structure of LaTeX libraries (.sty and .cls files).
 *
 * @author Ezrnest
 */
@Service(Service.Level.PROJECT)
class LatexLibraryStructureService(
    private val project: Project
) : AbstractBlockingCacheService<String, LatexLibraryInfo?>() {
    companion object {
        val performanceTracker = SimplePerformanceTracker()

        private val libraryCommandNameToExt: Map<String, String> = mapOf(
            "\\usepackage" to ".sty",
            "\\RequirePackage" to ".sty",
            "\\documentclass" to ".cls",
            "\\LoadClass" to ".cls"
        )

        // never expire unless invalidated manually
        private val LIBRARY_FILESET_EXPIRATION_TIME = Duration.INFINITE

        fun getInstance(project: Project): LatexLibraryStructureService {
            return project.service()
        }
    }

    override fun computeValue(key: String, oldValue: LatexLibraryInfo?): LatexLibraryInfo? {
        return performanceTracker.track {
            computePackageFilesetsRecur(key, mutableSetOf())
        }
    }

    private fun getLibraryName(current: VirtualFile, project: Project): String {
        val fileName = current.name
        if (current.fileType == StyleFileType) {
            val name = NewCommandsIndex.getByName("\\ProvidesPackage", project, current).firstNotNullOfOrNull {
                it.requiredParameterText(0)
            } ?: return fileName
            return "$name.sty"
        }
        if (current.fileType == ClassFileType) {
            val name = NewCommandsIndex.getByName("\\ProvidesClass", project, current).firstNotNullOfOrNull {
                it.requiredParameterText(0)
            } ?: return fileName
            return "$name.cls"
        }
        return fileName
    }

    private fun computePackageFilesetsRecur(nameWithExt: String, processing: MutableSet<String>): LatexLibraryInfo? {
        if (!processing.add(nameWithExt)) return null // Prevent infinite recursion
        getUpToDateValueOrNull(nameWithExt, LIBRARY_FILESET_EXPIRATION_TIME)?.let {
            return it // Return cached value if available
        }

        val path = LatexPackageLocation.getPackageLocation(nameWithExt, project) ?: run {
            Log.info("LatexLibrary not found!! $nameWithExt")
            return null
        }
        val file = LocalFileSystem.getInstance().findFileByNioFile(path) ?: return null
        val allFiles = mutableSetOf(file)
        val allPackages = mutableSetOf(nameWithExt)
        val directDependencies = mutableSetOf<String>()
        val commands = NewSpecialCommandsIndex.getPackageIncludes(project, file)
        for (command in commands) {
            val packageText = command.requiredParameterText(0) ?: continue
            val ext = libraryCommandNameToExt[command.name] ?: continue
            val refTexts = mutableListOf<String>()
            val refInfos = mutableListOf<Set<VirtualFile>>()
            for (text in packageText.split(',')) {
                val trimmed = text.trim()
                if (trimmed.isEmpty()) continue
                val name = trimmed + ext
                directDependencies.add(name)
                if (name in allPackages) continue // prevent infinite recursion
                val info = computePackageFilesetsRecur(name, processing)
                info?.let {
                    refTexts.add(trimmed)
                    refInfos.add(setOf(it.location))
                    allFiles.addAll(it.files)
                    allPackages.addAll(it.allIncludedPackageNames)
                }
            }
            command.putUserData(
                LatexProjectStructure.userDataKeyFileReference,
                CacheValueTimed(refTexts to refInfos)
            )
        }
        val otherPackages = LatexRegexBasedIndex.getPackageInclusions(file, project)
        otherPackages.forEach {
            val name = "$it.sty"
            directDependencies.add(name)
            if (name in allPackages) return@forEach
            val info = computePackageFilesetsRecur(name, processing)
            info?.let {
                allFiles.addAll(info.files)
                allPackages.addAll(info.allIncludedPackageNames)
            }
        }
        val info = LatexLibraryInfo(LatexLib.fromFileName(nameWithExt), file, allFiles, directDependencies, allPackages)
        putValue(nameWithExt, info)
        Log.info("LatexLibrary Loaded: $nameWithExt")
        return info
    }

    fun getLibraryInfo(nameWithExt: String): LatexLibraryInfo? {
        return getOrComputeNow(nameWithExt, LIBRARY_FILESET_EXPIRATION_TIME)
    }

    fun getLibraryInfo(name: LatexLib): LatexLibraryInfo? {
        if (name.isCustom) return null // Custom libraries are not supported
        return getLibraryInfo(name.name)
    }

    fun getLibraryInfo(path: Path): LatexLibraryInfo? {
        if (path.nameCount != 1) return null
        return getLibraryInfo(path.fileName.pathString)
    }

    fun invalidateLibraryCache() {
        clearAllCache()
    }

    fun librarySize(): Int {
        return caches.size
    }
}

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
    val userDataKeyFileReference = Key.create<
        CacheValueTimed<
            Pair<List<String>, List<Set<VirtualFile>>> // List of pairs of original text and set of files in order
            >
        >("latex.command.reference.files")

    const val CMD_DOCUMENT_CLASS = "\\documentclass"
    const val CMD_ADD_TO_LUATEX_PATH = "\\addtoluatexpath"
    const val CMD_EXTERNAL_DOCUMENT = "\\externaldocument"
    const val CMD_DECLARE_GRAPHICS_EXTENSIONS = "\\DeclareGraphicsExtensions"

    fun getPossibleRootFiles(project: Project): Set<VirtualFile> {
        if (DumbService.isDumb(project)) return emptySet()
        val rootFiles = mutableSetOf<VirtualFile>()

        val documentCommands = NewCommandsIndex.getByName("\\documentclass", project)
        documentCommands.filter { it.requiredParameterText(0) != null }
            .mapNotNullTo(rootFiles) { it.containingFile.virtualFile }

        project.getLatexRunConfigurations().forEach {
            it.mainFile?.let { mainFile -> rootFiles.add(mainFile) }
        }
        FilenameIndex.getVirtualFilesByName("main.tex", true, project.contentSearchScope)
            .filter { it.isValid }
            .forEach { rootFiles.add(it) }

        return rootFiles
    }

    fun isLatexLibraryFile(file: VirtualFile, project: Project): Boolean {
        // Check if the file is a library file, e.g. in the texlive distribution
        val filetype = file.fileType
        return (filetype == StyleFileType || filetype == ClassFileType || filetype == LatexSourceFileType) &&
            !ProjectFileIndex.getInstance(project).isInProject(file)
    }

    private open class ProjectInfo(
        val project: Project,
        var rootDirs: Set<VirtualFile>,
        val bibInputPaths: Set<VirtualFile>,
        val timestamp: Instant = Clock.System.now()
    )

    private fun Path.findVirtualFile(): VirtualFile? = LocalFileSystem.getInstance().findFileByNioFile(this)

    /**
     * Temporary information for building a fileset.
     */
    private class FilesetProcessor(
        project: Project,
        rootDirs: MutableSet<VirtualFile>, bibInputPaths: MutableSet<VirtualFile>,
        timestamp: Instant,
        val root: VirtualFile,
    ) : ProjectInfo(project, rootDirs, bibInputPaths, timestamp) {
        /**
         * The current root directory, particularly for subfiles package as each `subfile` assigns a new root directory.
         */
        var currentRootDir: VirtualFile? = root.parent

        /**
         * All files in the fileset in encountering order.
         */
        val files: LinkedHashSet<VirtualFile> = linkedSetOf()

        init {
            files.add(root)
        }

        val libraries: MutableSet<String> = mutableSetOf()
        var declareGraphicsExtensions: Set<String>? = null
        var graphicsSuffix: Set<Path> = emptySet()
        var luatexPaths: Set<VirtualFile> = emptySet()

        private fun extractExternalDocumentInfoInFileset(allFilesScope: GlobalSearchScope): List<ExternalDocumentInfo> {
            val externalDocumentCommands = NewCommandsIndex.getByName(
                LatexGenericRegularCommand.EXTERNALDOCUMENT.commandWithSlash,
                allFilesScope.restrictedByFileTypes(LatexFileType)
            )
            if (externalDocumentCommands.isEmpty()) return emptyList()

            val result = mutableListOf<ExternalDocumentInfo>()
            for (command in externalDocumentCommands) {
                val pathText = command.requiredParameterText(0) ?: continue
                val path = pathOrNull("$pathText.tex") ?: continue
                val prefix = command.optionalParameterText(0) ?: ""
                val files = FilenameIndex.getVirtualFilesByName(path.name, true, project.projectSearchScope).toSet()
                if (files.isEmpty()) continue
                result.add(ExternalDocumentInfo(prefix, files))
            }
            return result
        }

        fun createFileset(): Fileset {
            val scope = GlobalSearchScope.filesWithLibrariesScope(project, files)
            val extInfo = extractExternalDocumentInfoInFileset(scope)
            return Fileset(root, files, libraries, scope, extInfo)
        }

        private fun processNext(v: VirtualFile) {
            if (files.add(v)) {
                // new element added, continue building the fileset
                recursiveBuildFileset(v)
            }
        }

        private fun addLibrary(info: LatexLibraryInfo) {
            files.addAll(info.files)
            libraries.addAll(info.allIncludedPackageNames)
        }

        private inline fun processElementsWithPaths0(
            elements: List<Sequence<Path>>, refInfoList: List<MutableSet<VirtualFile>>,
            crossinline findFiles: (Path) -> Collection<VirtualFile>
        ) {
            for ((paths, refInfo) in elements.zip(refInfoList)) {
                for (path in paths) {
                    for (file in findFiles(path)) {
                        processNext(file)
                        refInfo.add(file)
                    }
                }
            }
        }

        private inline fun processElementsWithPaths(
            elements: List<Sequence<Path>>, refInfoList: List<MutableSet<VirtualFile>>,
            crossinline findFile: (Path) -> VirtualFile?
        ) {
            for ((paths, refInfo) in elements.zip(refInfoList)) {
                for (path in paths) {
                    findFile(path)?.let { file ->
                        processNext(file)
                        refInfo.add(file)
                    }
                }
            }
        }

        private fun processLibraryReferences(
            elements: List<Sequence<Path>>, refInfoList: List<MutableSet<VirtualFile>>
        ) {
            for ((paths, refInfo) in elements.zip(refInfoList)) {
                for (path in paths) {
                    val libraryInfo = LatexLibraryStructureService.getInstance(project).getLibraryInfo(path)
                    if (libraryInfo != null) {
                        addLibrary(libraryInfo)
                        refInfo.add(libraryInfo.location)
                    }
                    else {
                        // even though we cannot find the library, we still add it to the libraries set
                        // so that the definition service can still find it
                        libraries.add(path.name)
                    }
                }
            }
        }

        private fun processFilesUnderRootDirs(
            elements: List<Sequence<Path>>, refInfoList: List<MutableSet<VirtualFile>>, rootDirs: Collection<VirtualFile>,
        ) {
            processElementsWithPaths(elements, refInfoList) { path ->
                if (path.isAbsolute) path.findVirtualFile()
                else {
                    val pathString = path.invariantSeparatorsPathString
                    currentRootDir?.findFileByRelativePath(pathString) ?: rootDirs.firstNotNullOfOrNull { sourcePath ->
                        sourcePath.findFileByRelativePath(pathString)
                    }
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
            text: String, command: LatexCommands, file: VirtualFile
        ): String {
            var result = expandCommandsOnce(text, project, file)
            result = result.trim()
            return result
        }

        private fun resolveSubfolder(root: VirtualFile, siblingRelativePath: String): VirtualFile? {
            return runCatching { root.parent?.findDirectory(siblingRelativePath) }.getOrNull()
        }

        private fun asFileInputCtx(intro: LatexContextIntro): SimpleFileInputContext? {
            if (intro !is LatexContextIntro.Assign) return null
            val contexts = intro.contexts
            for (ctx in contexts) {
                if (ctx is SimpleFileInputContext) return ctx
            }
            return null
        }

        private fun findReferredFiles(
            command: LatexCommands, file: VirtualFile,
        ) {
            val info = this
            // remark: we should only use stub-based information here for performance reasons
            val commandName = command.nameWithSlash ?: return
            val reqParamTexts = command.requiredParametersText()

            val semantics = AllPredefined.lookupCommand(commandName.removePrefix("\\")) ?: return
            // we use predefined commands here because custom commands are not ready yet
            val dependency = semantics.dependency

            // If the command is a graphics command, we can use the declared graphics extensions
            val configuredExtensions = if (dependency == LatexLib.GRAPHICX) info.declareGraphicsExtensions else null

            // let us locate the sub
            val rangesAndTextsWithExt: MutableList<Pair<List<String>, Set<String>>> = mutableListOf()
            // Special case for the subfiles package: the (only) mandatory optional parameter should be a path to the main file
            // We reference it because we include the preamble of that file, so it is in the file set (partially)
            if (commandName == CMD_DOCUMENT_CLASS && reqParamTexts.any { it.endsWith(SUBFILES.name) }) {
                // try to find the main file in the optional parameter map
                command.optionalParameterTextMap().entries.firstOrNull()?.let { (k, _) ->
                    // the value should be empty, we only care about the key, see Latex.bnf
                    rangesAndTextsWithExt.add(
                        listOf(k) to setOf("tex")
                    )
                }
            }
            semantics.arguments.filter { it.isRequired }.zip(reqParamTexts).forEach { (argument, contentText) ->
                val ctx = asFileInputCtx(argument.contextSignature) ?: return@forEach
                if (contentText.contains("\\subfix")) {
                    // \input{\subfix{file.tex}}
                    // do not deal with \input, but leave it to the \subfix command
                    return@forEach
                }
                val paramTexts = if (ctx.isCommaSeparated) {
                    contentText.split(PatternMagic.parameterSplit).filter { it.isNotBlank() }
                }
                else listOf(contentText)

                val extensions = configuredExtensions ?: ctx.supportedExtensions
                rangesAndTextsWithExt.add(paramTexts to extensions)
            }

            val extractedRefTexts = rangesAndTextsWithExt.flatMap { it.first }

            var pathWithExts = rangesAndTextsWithExt.flatMap { (paramTexts, extensions) ->
                val noExtensionProvided = extensions.isEmpty()
                val extensionSeq = extensions.asSequence()
                paramTexts.asSequence().map { text ->
                    val text = pathTextExtraProcessing(text, command, file)
                    pathOrNull(text)?.let { path ->
                        if (path.extension.isNotEmpty() || noExtensionProvided) {
                            sequenceOf(path)
                        }
                        else {
                            path.name.let { fileName ->
                                extensionSeq.map { ext -> path.resolveSibling("$fileName.$ext") }
                            }
                        }
                    } ?: emptySequence()
                }
            }
            val refInfos: List<MutableSet<VirtualFile>> = List(extractedRefTexts.size) { mutableSetOf() }

            var searchDirs = info.rootDirs
            val oldRootDir = this.currentRootDir

            when (commandName) {
                "\\tikzfig", "\\ctikzfig" -> {
                    searchDirs = searchDirs + searchDirs.mapNotNull { runCatching { it.findDirectory("figures") }.getOrNull() }
                }

                in CommandMagic.absoluteImportCommands -> {
                    command.requiredParameterText(0)?.let {
                        currentRootDir = resolveSubfolder(root, it)
                    }
                }

                in CommandMagic.relativeImportCommands -> {
                    command.requiredParameterText(0)?.let {
                        currentRootDir = resolveSubfolder(file, it)
                    }
                }
            }

            if (dependency in CommandMagic.graphicLibs && info.graphicsSuffix.isNotEmpty()) {
                val graphicsSuffix = info.graphicsSuffix.asSequence()
                pathWithExts = pathWithExts.map { paths ->
                    val allPathWithSuffix = paths.flatMap { path -> graphicsSuffix.map { suffix -> suffix.resolve(path) } }
                    (paths + allPathWithSuffix)
                }
            }

            var processPlainFilePath = true

            if (commandName in CommandMagic.packageInclusionCommands) {
                processLibraryReferences(pathWithExts, refInfos)
            }
            if (commandName in CommandMagic.bibliographyIncludeCommands) {
                // For bibliography files, we can search in the bib input paths
                processFilesUnderRootDirs(pathWithExts, refInfos, info.bibInputPaths)
            }
            if (commandName == CMD_EXTERNAL_DOCUMENT) {
                // \externaldocument uses the .aux file in the output directory, we are only interested in the source file,
                // but it can be anywhere (because no relative path will be given, as in the output directory everything will be on the same level).
                // This does not count for building the file set, because the external document is not actually in the fileset, only the label definitions are,
                for ((paths, refInfo) in pathWithExts.zip(refInfos)) {
                    for (path in paths) {
                        // try to find everywhere in the project
                        FilenameIndex.getVirtualFilesByName(path.fileName.pathString, true, info.project.projectSearchScope).forEach { file ->
                            refInfo.add(file)
                        }
                    }
                }
                // we do not add the file to the fileset, as it is not actually in the fileset,
                // but we still leave a reference to it
                processPlainFilePath = false // do not process more
            }
            if (processPlainFilePath)
                processFilesUnderRootDirs(pathWithExts, refInfos, searchDirs)

            val savedData = extractedRefTexts to refInfos
            updateOrMergeRefData(command, savedData)

            currentRootDir = oldRootDir
        }

        private fun addGraphicsPathsfo(file: VirtualFile) {
            // Declare graphics extensions
            NewCommandsIndex.getByName(CMD_DECLARE_GRAPHICS_EXTENSIONS, project, file)
                .lastOrNull()?.requiredParameterText(0)?.split(",")
                // Graphicx requires the dot to be included
                ?.map { it.trim(' ', '.') }?.let {
                    declareGraphicsExtensions = it.toSet()
                }

            NewCommandsIndex.getByNames(CommandMagic.graphicPathsCommandNames, project, file)
                .lastOrNull()?.getGraphicsPaths()?.mapNotNull { pathOrNull(it) }
                ?.let {
                    graphicsSuffix = it.toSet()
                }
        }

        private fun addLuatexPaths(project: Project, file: VirtualFile) {
            // addtoluatexpath
            val direct = NewCommandsIndex.getByName(CMD_ADD_TO_LUATEX_PATH, project, file)
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
                luatexPaths = luatexPaths + directories
            }
        }

        /**
         * Add new information such as declared graphics extensions and luatex paths to the given fileset info,
         * perform the given callback, and then restore the original information.
         */
        private inline fun withNewInformation(file: VirtualFile, callback: () -> Unit) {
            val info = this
            addGraphicsPathsfo(file)
            addLuatexPaths(project, file)

            val docClass = NewCommandsIndex.getByName(CMD_DOCUMENT_CLASS, project, file)
                .lastOrNull()?.requiredParameterText(0)
            val oldRoot = info.currentRootDir
            if (docClass != null && docClass.endsWith(SUBFILES.name)) {
                // subfiles package sets the root directory to the parent of the file
                info.currentRootDir = file.parent
            }

            callback()

            info.currentRootDir = oldRoot
        }

        private fun updateOrMergeRefData(command: PsiElement, refInfoList: Pair<List<String>, List<MutableSet<VirtualFile>>>) {
            val existingRef = command.getUserData(userDataKeyFileReference)
            val info = this
            if (existingRef == null || existingRef.timestamp < info.timestamp) {
                // overwrite the existing reference
                command.putUserData(userDataKeyFileReference, CacheValueTimed(refInfoList, info.timestamp))
            }
            else if (existingRef.timestamp == info.timestamp) {
                // If the marker is the same, update the files
                val existingFiles = existingRef.value.second
                val (names, newFiles) = refInfoList
                val updatedFiles = if (newFiles.size != existingFiles.size) {
                    // this should not happen as the parsing process is the same, but just in case
                    refInfoList
                }
                else {
                    names to List(newFiles.size) { index ->
                        newFiles[index] + existingFiles[index]
                    }
                }
                command.putUserData(userDataKeyFileReference, CacheValueTimed(updatedFiles, info.timestamp))
            }
            // remark: as it is guaranteed by the cache service that the fileset update will not run in parallel with itself,
            // we can safely update the user data without interference
        }

        /**
         * Gets the directly referred files from the given file relative to the given root file.
         *
         * Note: the path of the referred file is relative to the root file, not the file that contains the input command.
         */
        fun recursiveBuildFileset(file: VirtualFile) {
            // indeed, we may deal with the same file multiple times with different roots
            if (!file.isValid) return
            withNewInformation(file) {
                val fileInputCommands = NewSpecialCommandsIndex.getAllFileInputs(project, file)
                fileInputCommands.forEach {
                    findReferredFiles(it, file)
                }
            }
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

    private suspend fun buildFilesetsSuspend(project: Project, previous: LatexProjectFilesets?): LatexProjectFilesets {
        val newFileset = smartReadAction(project) {
            performanceTracker.track {
                buildFilesets(project)
            }
        }
        if (!ApplicationManager.getApplication().isUnitTestMode && newFileset != previous) {
            // refresh the inspections
            DaemonCodeAnalyzer.getInstance(project).restart()
            // there will be an exception if we try to restart the daemon in unit tests
            // see FileStatusMap.CHANGES_NOT_ALLOWED_DURING_HIGHLIGHTING
        }
        return newFileset
    }

    /**
     * Calls to update the filesets for the given project.
     * This will ensure that the filesets are recomputed and up-to-date.
     */
    suspend fun updateFilesetsSuspend(project: Project): LatexProjectFilesets {
        return TexifyProjectCacheService.getInstance(project).ensureRefresh(CACHE_KEY, ::buildFilesetsSuspend)
    }

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
    fun isProjectFilesetsAvailable(project: Project): Boolean {
        return getFilesets(project) != null
    }

    fun getFilesetDataFor(virtualFile: VirtualFile, project: Project): FilesetData? {
        return getFilesets(project)?.getData(virtualFile)
    }

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
    catch (e: InvalidPathException) {
        null
    }
}

fun GlobalSearchScope.restrictedByFileTypes(vararg fileTypes: FileType): GlobalSearchScope {
    return GlobalSearchScope.getScopeRestrictedByFileTypes(this, *fileTypes)
}
