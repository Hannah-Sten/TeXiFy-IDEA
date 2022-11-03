package nl.hannahsten.texifyidea.util.files

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
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.magic.FileMagic
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
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
fun Document.psiFile(project: Project): PsiFile? = PsiDocumentManager.getInstance(project).getPsiFile(this)

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
fun getExternalFile(path: String): VirtualFile? =
    LocalFileSystem.getInstance().findFileByPath(path)

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