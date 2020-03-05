package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.index.LatexEnvironmentsIndex
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.lang.Package
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.*
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
object FileUtil {

    /**
     * Matches the extension of a file name, including the dot.
     */
    val FILE_EXTENSION = Pattern.compile("\\.[^.]+$")!!

    /**
     * Matches the file body, including the dot
     */
    val FILE_BODY = Pattern.compile(".*\\.")!!

    /**
     * Get the FileType instance that corresponds to the given file extension.
     *
     * @param extensionWithoutDot
     *         The file extension to get the corresponding FileType instance of without a dot in
     *         front.
     * @return The corresponding FileType instance.
     */
    @JvmStatic
    fun fileTypeByExtension(extensionWithoutDot: String): FileType {
        return Magic.File.fileTypes.firstOrNull {
            it.defaultExtension == extensionWithoutDot
        } ?: LatexFileType
    }

    /**
     * Retrieves the file path relative to the root path, or `null` if the file is not a child
     * of the root.
     *
     * @param rootPath
     *         The path of the root
     * @param filePath
     *         The path of the file
     * @return The relative path of the file to the root, or `null` if the file is no child of
     * the root.
     */
    @JvmStatic
    fun pathRelativeTo(rootPath: String, filePath: String): String? {
        if (!filePath.startsWith(rootPath)) {
            return null
        }
        return filePath.substring(rootPath.length)
    }
}

/**
 * Get the file search scope for this psi file.
 */
val PsiFile.fileSearchScope: GlobalSearchScope
    get() = GlobalSearchScope.fileScope(this)

/**
 * Looks up the PsiFile that corresponds to the Virtual File.
 */
fun VirtualFile.psiFile(project: Project): PsiFile? {
    if (!this.isValid) return null
    return PsiManager.getInstance(project).findFile(this)
}

fun VirtualFile.findVirtualFileByAbsoluteOrRelativePath(filePath: String): VirtualFile? {
    val isAbsolute = File(filePath).isAbsolute
    return if (!isAbsolute) {
        findFileByRelativePath(filePath)
    }
    else {
        LocalFileSystem.getInstance().findFileByPath(filePath)
    }
}

/**
 * Looks for a certain file, relative to this directory or if the given path is absolute use that directly.
 *
 * First looks if the file including extensions exists, when it doesn't it tries to append all
 * possible extensions until it finds a good one.
 *
 * @param filePath
 *         The name of the file relative to the directory, or an absolute path.
 * @param extensions
 *         Set of all supported extensions to look for.
 * @return The matching file, or `null` when the file couldn't be found.
 */
fun VirtualFile.findFile(filePath: String, extensions: Set<String> = emptySet()): VirtualFile? {
    val isAbsolute = File(filePath).isAbsolute
    var file = if (!isAbsolute) {
        findFileByRelativePath(filePath)
    }
    else {
        LocalFileSystem.getInstance().findFileByPath(filePath)
    }
    if (file != null && !file.isDirectory) return file

    extensions.forEach { extension ->
        val lookFor = if (filePath.endsWith(".$extension")) filePath else "$filePath.$extension"
        file = if (!isAbsolute) {
            findFileByRelativePath(lookFor)
        }
        else {
            LocalFileSystem.getInstance().findFileByPath(lookFor)
        }

        if (file != null && !file!!.isDirectory) return file
    }

    return null
}

/**
 * Recursively finds all files in a directory (thus, also the files in sub-directories etc.)
 */
fun VirtualFile.allChildFiles(): Set<VirtualFile> {
    val set = HashSet<VirtualFile>()
    allChildFiles(set)
    return set
}

/**
 * Recursive implementation of [allChildFiles].
 */
private fun VirtualFile.allChildFiles(files: MutableSet<VirtualFile>) {
    if (isDirectory) {
        children.forEach {
            it.allChildFiles(files)
        }
    }
    else files.add(this)
}

/**
 * Removes the extension from a given file name.
 */
fun String.removeFileExtension() = FileUtil.FILE_EXTENSION.matcher(this).replaceAll("")!!

/**
 * Returns the extension of given filename
 */
fun String.getFileExtension(): String = if (this.contains(".")) FileUtil.FILE_BODY.matcher(this).replaceAll("")!! else ""

/**
 * Creates a project directory at `path` which will be marked as excluded.
 *
 * @param path The path to create the directory to.
 */
fun Module.createExcludedDir(path: String) {
    ModuleRootManager.getInstance(this).modifiableModel.addContentEntry(path)
            .addExcludeFolder(path)
}

/**
 * Looks for all file inclusions in a given file, excluding installed LaTeX packages.
 *
 * @return A list containing all included files.
 */
fun PsiFile.findInclusions(): List<PsiFile> {
    return LatexIncludesIndex.getItems(this)
            .flatMap { it.getIncludedFiles(false) }
            .toList()
}

/**
 * Checks if the file has LaTeX syntax.
 */
fun PsiFile.isLatexFile() = fileType == LatexFileType ||
        fileType == StyleFileType || fileType == ClassFileType

/**
 * Checks if the file has a `.sty` extention. This is a workaround for file type checking.
 */
fun PsiFile.isStyleFile() = virtualFile.extension == "sty"

/**
 * Checks if the file has a `.cls` extention. This is a workaround for file type checking.
 */
fun PsiFile.isClassFile() = virtualFile.extension == "cls"

/**
 * Looks up the argument that is in the documentclass command, and if the file is found in the project return it.
 * Note this explicitly does not find files elsewhere on the system.
 */
fun PsiFile.documentClassFileInProject(): PsiFile? {
    val command = commandsInFile().asSequence()
            .filter { it.name == "\\documentclass" }
            .firstOrNull() ?: return null
    val argument = command.requiredParameter(0) ?: return null
    return findFile("$argument.cls")
}

/**
 * Checks if the given package is included in the file set.
 *
 * @param packageName
 *          The name of the package to check for.
 * @return `true` when there is a package with name `packageName` in the file set, `false` otherwise.
 */
fun PsiFile.isUsed(packageName: String) = PackageUtils.getIncludedPackages(this).contains(packageName)

/**
 * Checks if the given package is included into the file set.
 *
 * @param `package`
 *          The package to check for.
 * @return `true` when there is a package `package` included in the file set, `false` otherwise.
 */
@Suppress("unused")
fun PsiFile.isUsed(`package`: Package) = isUsed(`package`.name)

/**
 * Scans the whole document (recursively) for all referenced/included files, except installed LaTeX packages.
 * Never use this directly, use the cached [referencedFileSet] instead.
 *
 * @return A collection containing all the PsiFiles that are referenced from this file.
 */
internal fun PsiFile.referencedFiles(rootFile: VirtualFile): Set<PsiFile> {
    val result = HashSet<PsiFile>()
    referencedFiles(result, rootFile)
    return result
}

/**
 * Recursive implementation of [referencedFiles].
 */
private fun PsiFile.referencedFiles(files: MutableCollection<PsiFile>, rootFile: VirtualFile) {
    LatexIncludesIndex.getItems(project, fileSearchScope).forEach command@{ command ->
        command.references.filterIsInstance<InputFileReference>()
                .mapNotNull { it.resolve(false, rootFile) }
                .forEach {
                    // Do not re-add all referenced files if we already did that
                    if (it in files) return@forEach
                    files.add(it)
                    it.referencedFiles(files, rootFile)
                }
    }
}

/**
 * Looks up a file relative to this file.
 *
 * @param path
 *         The path relative to this file.
 * @return The found file, or `null` when the file could not be found.
 */
fun PsiFile.findFile(path: String, extensions: Set<String>? = null): PsiFile? {
    val directory = containingDirectory.virtualFile

    val file = directory.findFile(path, extensions
            ?: Magic.File.includeExtensions)
            ?: return scanRoots(path, extensions)
    val psiFile = PsiManager.getInstance(project).findFile(file)
    if (psiFile == null || LatexFileType != psiFile.fileType &&
            StyleFileType != psiFile.fileType &&
            BibtexFileType != psiFile.fileType) {
        return scanRoots(path, extensions)
    }

    return psiFile
}

/**
 * [findFile] but then it scans all content roots.
 *
 * @param path
 *         The path relative to {@code original}.
 * @return The found file, or `null` when the file could not be found.
 */
fun PsiFile.scanRoots(path: String, extensions: Set<String>? = null): PsiFile? {
    val rootManager = ProjectRootManager.getInstance(project)
    rootManager.contentSourceRoots.forEach { root ->
        val file = root.findFile(path, extensions
                ?: Magic.File.includeExtensions)
        if (file != null) {
            return file.psiFile(project)
        }
    }

    return null
}

/**
 * Get the corresponding document of the PsiFile.
 */
fun PsiFile.document(): Document? = PsiDocumentManager.getInstance(project).getDocument(this)

/**
 * @see [LatexCommandsIndex.getItems]
 */
fun PsiFile.commandsInFile(): Collection<LatexCommands> = LatexCommandsIndex.getItems(this)

/**
 * @see [LatexEnvironmentsIndex.getItems]
 */
fun PsiFile.environmentsInFile(): Collection<LatexEnvironment> = LatexEnvironmentsIndex.getItems(this)

/**
 * Get the editor of the file if it is currently opened.
 */
fun PsiFile.openedEditor() = FileEditorManager.getInstance(project).selectedTextEditor

/**
 * Get all the definitions in the file.
 */
fun PsiFile.definitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this)
            .filter { it.isDefinition() }
}

/**
 * Get all the definitions and redefinitions in the file.
 */
@Suppress("unused")
fun PsiFile.definitionsAndRedefinitions(): Collection<LatexCommands> {
    return LatexDefinitionIndex.getItems(this)
}

/**
 * Retrieves the [PsiFile] for the document within the given [project].
 *
 * @param project
 *          The project scope to retrieve the psi file for.
 * @return The PSI file matching the document, or `null` when the PSI file could not be found.
 */
fun Document.psiFile(project: Project): PsiFile? = PsiDocumentManager.getInstance(project).getPsiFile(this)

/**
 * Creates a new file with a given name and given content.
 *
 * Also checks if the file already exists, and modifies the name accordingly.
 *
 * @return The created file.
 */
fun createFile(fileName: String, contents: String): File {
    var count = 0
    var currentFileName = fileName
    while (File(currentFileName).exists()) {
        val extension = "." + FileUtilRt.getExtension(currentFileName)
        var stripped = currentFileName.substring(0, currentFileName.length - extension.length)

        val countString = count.toString()
        if (stripped.endsWith(countString)) {
            stripped = stripped.substring(0, stripped.length - countString.length)
        }

        currentFileName = stripped + (++count) + extension
    }

    return File(currentFileName).apply {
        createNewFile()
        LocalFileSystem.getInstance().refresh(true)
        writeText(contents, StandardCharsets.UTF_8)
    }
}

/**
 * Get a(n external) file by its absolute path.
 */
fun getExternalFile(path: String): VirtualFile? =
        LocalFileSystem.getInstance().findFileByPath(path)