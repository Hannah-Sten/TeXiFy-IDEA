package nl.hannahsten.texifyidea.index.projectstructure

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.datetime.Instant
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider.getGraphicsPaths
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.index.projectstructure.LatexProjectStructure.findVirtualFile
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.nameWithSlash
import nl.hannahsten.texifyidea.util.CacheValueTimed
import nl.hannahsten.texifyidea.util.expandCommandsOnce
import nl.hannahsten.texifyidea.util.files.allChildDirectories
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PackageMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.projectSearchScope
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Temporary information for building a fileset.
 */
class FilesetProcessor(
    project: Project,
    rootDirs: MutableSet<VirtualFile>,
    bibInputPaths: MutableSet<VirtualFile>,
    timestamp: Instant,
    val root: VirtualFile,
) : LatexProjectStructure.ProjectInfo(project, rootDirs, bibInputPaths, timestamp) {
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
            CommandNames.EXTERNAL_DOCUMENT,
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
        elements: List<Sequence<Path>>, refInfoList: List<MutableSet<VirtualFile>>, command: LatexCommands
    ) {
        for ((paths, refInfo) in elements.zip(refInfoList)) {
            for (path in paths) {
                // check local package first, since LaTeX will resolve to the local package if it exists
                val localFile = findLocalFile(path, rootDirs)

                if (localFile != null) {
                    processNext(localFile)
                    refInfo.add(localFile)
                    continue
                }

                // check library package if local is not present
                addSingleLibrary(path, refInfo)

                // sideloading packages
                PackageMagic.packagesLoadedWithOptions[LatexLib.Package(path.nameWithoutExtension)]?.let { loadedPackages ->
                    // This is expensive, but since we only do this for every \usepackage{biblatex} the performance impact is hopefully limited
                    val options = command.getOptionalParameterMap().mapKeys { it.key.text }.mapValues { it.value?.text }
                    loadedPackages.map { entry ->
                        // Can be specified as key without value or key=true, but key=false disables the option
                        if (options.contains(entry.key) && options[entry.key] != "false") {
                            addSingleLibrary(Path(entry.value.name + ".sty"), refInfo)
                        }
                    }
                }
            }
        }
    }

    private fun addSingleLibrary(
        path: Path,
        refInfo: MutableSet<VirtualFile>
    ): LatexLibraryInfo? {
        val libraryInfo = LatexLibraryStructureService.getInstance(project).getLibraryInfo(path, root)
        if (libraryInfo != null) {
            addLibrary(libraryInfo)
            refInfo.add(libraryInfo.location)
        }
        else {
            // even though we cannot find the library, we still add it to the libraries set
            // so that the definition service can still find it
            libraries.add(path.name)
        }
        return libraryInfo
    }

    private fun processFilesUnderRootDirs(
        elements: List<Sequence<Path>>, refInfoList: List<MutableSet<VirtualFile>>, rootDirs: Collection<VirtualFile>,
    ) {
        processElementsWithPaths(elements, refInfoList) { path ->
            findLocalFile(path, rootDirs)
        }
    }

    private fun findLocalFile(
        path: Path,
        rootDirs: Collection<VirtualFile>
    ): VirtualFile? = if (path.isAbsolute) path.findVirtualFile()
    else {
        val pathString = path.invariantSeparatorsPathString
        currentRootDir?.findFileByRelativePath(pathString) ?: rootDirs.firstNotNullOfOrNull { sourcePath ->
            sourcePath.findFileByRelativePath(pathString)
        }
    }

    private fun pathTextExtraProcessing(
        text: String, file: VirtualFile
    ): String {
        var result = expandCommandsOnce(text, project, file)
        result = result.trim()
        return result
    }

    private fun resolveSubfolder(root: VirtualFile, siblingRelativePath: String): VirtualFile? = runCatching { root.parent?.findDirectory(siblingRelativePath) }.getOrNull()

    private fun findReferencedFiles(
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
        if (commandName == CommandNames.DOCUMENT_CLASS && reqParamTexts.any { it.endsWith(LatexLib.SUBFILES.name) }) {
            // try to find the main file in the optional parameter map
            command.optionalParameterTextMap().entries.firstOrNull()?.let { (k, _) ->
                // the value should be empty, we only care about the key, see Latex.bnf
                rangesAndTextsWithExt.add(
                    listOf(k) to setOf("tex")
                )
            }
        }
        semantics.arguments.filter { it.isRequired }.zip(reqParamTexts).forEach { (argument, contentText) ->
            val ctx = LatexContexts.asFileInputCtx(argument.contextSignature) ?: return@forEach
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
                val text = pathTextExtraProcessing(text, file)
                pathOrNull(text)?.let { path ->
                    // If a file a.b.tex exists, \input{a.b} prefers a.b.tex over a.b
                    if (noExtensionProvided) {
                        sequenceOf(path)
                    }
                    else {
                        path.name.let { fileName ->
                            // For some commands, like \input, the extension is optional, for now we always try it
                            extensionSeq.map { ext -> path.resolveSibling("$fileName.$ext") } + listOf(path.resolveSibling(fileName))
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
            processLibraryReferences(pathWithExts, refInfos, command)
        }
        if (commandName in CommandMagic.bibliographyIncludeCommands) {
            // For bibliography files, we can search in the bib input paths
            processFilesUnderRootDirs(pathWithExts, refInfos, info.bibInputPaths)
        }
        if (commandName == CommandNames.EXTERNAL_DOCUMENT) {
            // \externaldocument uses the .aux file in the output directory, we are only interested in the source file,
            // but it can be anywhere (because no relative path will be given, as in the output directory everything will be on the same level).
            // This does not count for building the file set, because the external document is not actually in the fileset, only the label definitions are
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
        NewCommandsIndex.getByName(CommandNames.DECLARE_GRAPHICS_EXTENSIONS, project, file)
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
        val direct = NewCommandsIndex.getByName(CommandNames.ADD_TO_LUATEX_PATH, project, file)
            .mapNotNull { it.requiredParameterText(0) }
            .flatMap { it.split(",") }
        val viaUsepackage = NewSpecialCommandsIndex.getPackageIncludes(project, file)
            .filter { it.requiredParameterText(0) == LatexLib.ADDTOLUATEXPATH.name }
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

        val docClass = NewCommandsIndex.getByName(CommandNames.DOCUMENT_CLASS, project, file)
            .lastOrNull()?.requiredParameterText(0)
        val oldRoot = info.currentRootDir
        if (docClass != null && docClass.endsWith(LatexLib.SUBFILES.name)) {
            // subfiles package sets the root directory to the parent of the file
            info.currentRootDir = file.parent
        }

        callback()

        info.currentRootDir = oldRoot
    }

    private fun updateOrMergeRefData(command: PsiElement, refInfoList: Pair<List<String>, List<MutableSet<VirtualFile>>>) {
        val existingRef = command.getUserData(LatexProjectStructure.userDataKeyFileReference)
        val info = this
        if (existingRef == null || existingRef.timestamp < info.timestamp) {
            // overwrite the existing reference
            command.putUserData(LatexProjectStructure.userDataKeyFileReference, CacheValueTimed(refInfoList, info.timestamp))
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
            command.putUserData(LatexProjectStructure.userDataKeyFileReference, CacheValueTimed(updatedFiles, info.timestamp))
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
                findReferencedFiles(it, file)
            }
        }
    }
}