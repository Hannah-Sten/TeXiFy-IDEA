package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.util.appendExtension
import java.io.File

/**
 * Looks up the PsiFile that corresponds to the Virtual File.
 */
fun VirtualFile.psiFile(project: Project): PsiFile? {
    if (!this.isValid) return null
    return PsiManager.getInstance(project).findFile(this)
}

fun VirtualFile.findVirtualFileByAbsoluteOrRelativePath(filePath: String): VirtualFile? {
    if (filePath.isBlank()) return null
    val isAbsolute = File(filePath).isAbsolute
    return if (!isAbsolute) {
        findFileByRelativePath(filePath)
    }
    else {
        LocalFileSystem.getInstance().findFileByPath(filePath)
    }
}

/**
 * Try to find the virtual file, as absolute path or relative to a content root.
 */
fun findVirtualFileByAbsoluteOrRelativePath(path: String, project: Project): VirtualFile? {
    if (path.isBlank()) return null

    val fileSystem = LocalFileSystem.getInstance()

    val file = fileSystem.findFileByPath(path)
    if (file != null) {
        return file
    }
    else {
        // Maybe it is a relative path
        ProjectRootManager.getInstance(project).contentRoots.forEach { root ->
            root.findFileByRelativePath(path)?.let { return it }
        }
    }

    return null
}

/**
 * Looks for a certain file, relative to this directory or if the given path is absolute use that directly.
 *
 * When this is a file (instead of a directory) it doesn't find files that are in
 * the same directory as this file. When that is your goal, pass in the parent directory instead.
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
fun VirtualFile.findFile(filePath: String, extensions: List<String> = emptyList()): VirtualFile? {
    if (filePath.isBlank()) return null
    try {
        val isAbsolute = File(filePath).isAbsolute
        var file = if (!isAbsolute) {
            findFileByRelativePath(filePath)
        }
        else {
            LocalFileSystem.getInstance().findFileByPath(filePath)
        }
        if (file != null && !file.isDirectory && (extensions.isEmpty() || file.extension in extensions)) return file

        extensions.forEach { extension ->
            val lookFor = filePath.appendExtension(extension)
            file = if (!isAbsolute) {
                findFileByRelativePath(lookFor)
            }
            else {
                LocalFileSystem.getInstance().findFileByPath(lookFor)
            }

            if (file != null && !file!!.isDirectory) return file
        }
    }
    // #2248
    catch (ignored: InvalidVirtualFileAccessException) {}

    return null
}

/**
 * Find all child directories recursively, including [this] if it is a directory.
 */
fun VirtualFile.allChildDirectories(): Set<VirtualFile> {
    val set = mutableSetOf<VirtualFile>()
    allChildDirectories(set)
    return set
}

private fun VirtualFile.allChildDirectories(dirs: MutableSet<VirtualFile>) {
    if (isDirectory) {
        dirs.add(this)
        children.forEach {
            it.allChildDirectories(dirs)
        }
    }
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