package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.Magic
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