package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Document
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
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.util.magic.FileMagic
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.pathString

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
        return FileMagic.fileTypes.firstOrNull {
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
 * Removes the extension from a given file name.
 */
fun String.removeFileExtension() = FileUtil.FILE_EXTENSION.matcher(this).replaceAll("")!!

/**
 * Returns the extension of given filename
 */
fun String.getFileExtension(): String =
    if (this.contains(".")) FileUtil.FILE_BODY.matcher(this).replaceAll("")!! else ""

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
 * Retrieves the [PsiFile] for the document within the given [project].
 *
 * @param project
 *          The project scope to retrieve the psi file for.
 * @return The PSI file matching the document, or `null` when the PSI file could not be found.
 */
fun Document.psiFile(project: Project): PsiFile? = runReadAction { PsiDocumentManager.getInstance(project).getPsiFile(this) }

/**
 * Creates a new file with a given name and given content.
 *
 * Also checks if the file already exists, and modifies the name accordingly.
 *
 * @return The created file.
 */
fun createFile(fileName: String, contents: String): File {
    val currentFileName = getUniqueFileName(fileName)

    return File(currentFileName).apply {
        createNewFile()
        LocalFileSystem.getInstance().refresh(true)
        writeText(contents, StandardCharsets.UTF_8)
    }
}

/**
 * This writes to a file without using java.io.File
 *
 * Needs to be wrapped in a writable environment
 *
 * @param project The project this is for
 * @param filePath The absolute path of the file to create. File extension provided later
 * @param text The text to add to the file
 * @param root The root path of the project? Could possibly be replaced
 * @param extension The desired file extension **without** a dot, tex by default
 *
 * @return Returns the name of the file that was created without folder names, and with the extension
 */
fun writeToFileUndoable(project: Project, filePath: String, text: String, root: String, extension: String = "tex"): String {
    val filenameNoExtension = Path.of(filePath).fileName.toString()
    val filepathNoFilename = Path.of(filePath).parent.pathString

    // for windows
    val filepathNoFilenameForwardSeperators = filepathNoFilename.replace("\\", "/")

    // Create file...but not on fs yet
    val fileFactory = PsiFileFactory.getInstance(project)
    val newfile = fileFactory.createFileFromText(
        getUniqueFileName(
            filenameNoExtension.appendExtension(extension),
            filepathNoFilename
        ),
        LatexFileType,
        text
    )

    val projectRootManager = ProjectRootManager.getInstance(project)
    val allRoots = projectRootManager.contentRoots + projectRootManager.contentSourceRoots

    // The following is going to resolve the PsiDirectory that we need to add the new file to.
    var relativePath = ""
    var bestRoot: VirtualFile? = null
    for (testFile in allRoots) {
        val rootPath = testFile.path
        if (filepathNoFilenameForwardSeperators.startsWith(rootPath)) {
            relativePath = filepathNoFilenameForwardSeperators.substring(rootPath.length)
            bestRoot = testFile
            break
        }
    }
    if (bestRoot == null) {
        throw IOException("Can't find '$root' among roots")
    }

    val dirs = relativePath.split("/".toRegex()).dropLastWhile { it.isEmpty() }
        .toTypedArray()

    var resultDir: VirtualFile = bestRoot
    if (dirs.isNotEmpty()) {
        var i = 0
        if (dirs[0].isEmpty()) i = 1

        while (i < dirs.size) {
            var subdir = resultDir.findChild(dirs[i])
            if (subdir != null) {
                if (!subdir.isDirectory) {
                    throw IOException("Expected resultDir, but got non-resultDir: " + subdir.path)
                }
            }
            else {
                subdir = resultDir.createChildDirectory(LocalFileSystem.getInstance(), dirs[i])
            }
            resultDir = subdir
            i += 1
        }
    }

    // Actually create the file on fs
    val thing = PsiManager.getInstance(project).findDirectory(resultDir)?.add(newfile)

    // This is not a common case, use as a fallback
    if (thing !is PsiFile) {
        return filenameNoExtension.appendExtension(extension)
    }

    // back to your regularly scheduled programming. Does not cast to LatexFile because virtualFile inherits from PsiFile
    return thing.virtualFile.path
        .replace(File.separator, "/")
        .replace("$root/", "")
}

/**
 * Returns the name first non-conflicting filename with the provided base name
 */
fun getUniqueFileName(fileName: String, directory: String? = null): String {
    var count = 0
    var currentFileName = fileName
    while (
        File(
            (
                if (directory != null) {
                    directory + File.separator
                }
                else ""
                ) + currentFileName
        ).exists()
    ) {
        val extension = "." + FileUtilRt.getExtension(currentFileName)
        var stripped = currentFileName.substring(0, currentFileName.length - extension.length)

        val countString = count.toString()
        if (stripped.endsWith(countString)) {
            stripped = stripped.substring(0, stripped.length - countString.length)
        }

        currentFileName = stripped + (++count) + extension
    }
    return currentFileName
}

/**
 * Get a(n external) file by its absolute path.
 */
fun findFileByPath(path: String): VirtualFile? {
    // A blank path will resolve to the jetbrains /bin directory, which is likely not intended
    if (path.isBlank()) return null
    return LocalFileSystem.getInstance().findFileByPath(path)
}

/**
 * Converts the absolute path to a relative path.
 */
fun String.toRelativePath(basePath: String): String {
    return File(basePath).toURI().relativize(File(this).toURI()).path
}

/**
 * Extracts the list of files from the Drag and Drop Transferable, only if java file list is a supported flavour.
 *
 * @return The file list transfer data, or `null` when file lists are not supported.
 */
fun Transferable.extractFiles(): List<File>? {
    if (isDataFlavorSupported(DataFlavor.javaFileListFlavor).not()) return null

    @Suppress("UNCHECKED_CAST")
    return getTransferData(DataFlavor.javaFileListFlavor) as? List<File>
}

/**
 * Extracts the first file from the Drag and Drop Transferable, only if java file list is a supported flavour.
 *
 * @return The first file in the transfer data, or `null` when file lists are not supported.
 */
fun Transferable.extractFile() = extractFiles()?.firstOrNull()

/**
 * Looks up the relative path of the file represented by the given absolute path.
 * This relative path is relative to the content roots of this ProjectRootManager.
 *
 * @return The relative path, relative to all source roots. `null` when no relative path could be found.
 */
fun ProjectRootManager.relativizePath(absoluteFilePath: String): String? = contentSourceRoots.asSequence()
    .map { it to absoluteFilePath.toRelativePath(it.path) }
    .firstOrNull { (contentRoot, relativePath) ->
        // Make sure to convert to the right file when multiple files exist in the content
        // roots with the same name. Also convert to [File]s to normalize path names.
        val original = File(absoluteFilePath)
        val candidate = File("${contentRoot.path}/$relativePath")
        candidate.absolutePath == original.absolutePath
    }
    ?.second