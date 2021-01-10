package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.files.FileUtil
import nl.hannahsten.texifyidea.util.files.createExcludedDir
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import java.io.File

/**
 * Output file as a virtual file, or a promise to provide a path that can be constructed when the run configuration is actually created.
 * This allows for custom output paths in the run configuration template.
 *
 * Supported placeholders:
 * - $contentRoot
 * - $mainFile
 *
 * @param variant: out or auxil
 */
class LatexOutputPath(private val variant: String, var contentRoot: VirtualFile?, var mainFile: VirtualFile?, private val project: Project) {

    companion object {

        const val projectDirString = "{projectDir}"
        const val mainFileString = "{mainFileParent}"
    }

    fun clone(): LatexOutputPath {
        return LatexOutputPath(variant, contentRoot, mainFile, project).apply { if (this@LatexOutputPath.pathString.isNotBlank()) this.pathString = this@LatexOutputPath.pathString }
    }

    // Acts as a sort of cache
    var virtualFile: VirtualFile? = null

    var pathString: String = "$projectDirString/$variant"

    /**
     * Get the output path based on the values of [virtualFile] and [pathString], create it if it does not exist.
     */
    fun getAndCreatePath(): VirtualFile? {
        // No auxil directory should be present/created when there's no MiKTeX around, assuming that TeX Live does not support this
        if (!LatexSdkUtil.isMiktexAvailable && variant == "auxil") {
            return null
        }

        // Just to be sure, avoid using jetbrains /bin path as output
        if (pathString.isBlank()) {
            pathString = "$projectDirString/$variant"
        }

        // Caching of the result
        return getPath().also {
            virtualFile = it
        }
    }

    private fun getPath(): VirtualFile? {
        // When we previously made the mistake of calling findRelativePath with an empty string, the output path will be set to thee /bin folder of IntelliJ. Fix that here, to be sure
        if (virtualFile?.path?.endsWith("/bin") == true) {
            virtualFile = null
        }

        if (virtualFile != null) {
            return virtualFile!!
        }
        else {
            val pathString = if (pathString.contains(projectDirString)) {
                if (contentRoot == null) return if (mainFile != null) mainFile?.parent else null
                pathString.replace(projectDirString, contentRoot?.path ?: return null)
            }
            else {
                if (mainFile == null) return null
                pathString.replace(mainFileString, mainFile?.parent?.path ?: return null)
            }
            val path = LocalFileSystem.getInstance().findFileByPath(pathString)
            if (path != null && path.isDirectory) {
                return path
            }
            else {
                // Try to create the path
                createOutputPath(pathString)?.let { return it }
            }
            // Path is invalid (perhaps the user provided an invalid path)
            Notification("LaTeX", "Invalid output path", "Output path $pathString of the run configuration could not be created, trying default path ${contentRoot?.path + "/" + variant}", NotificationType.WARNING).notify(project)

            // Create and return default path
            if (contentRoot != null) {
                val defaultPathString = contentRoot!!.path + "/" + variant
                createOutputPath(defaultPathString)?.let { return it }
            }

            if (contentRoot != null) {
                return contentRoot!!
            }

            return null
        }
    }

    private fun getDefaultOutputPath(): VirtualFile? {
        if (mainFile == null) return null
        var defaultOutputPath: VirtualFile? = null
        runReadAction {
            val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile!!)
            if (moduleRoot?.path != null) {
                defaultOutputPath = LocalFileSystem.getInstance().findFileByPath(moduleRoot.path + "/" + variant)
            }
        }
        return defaultOutputPath
    }

    /**
     * Whether the current output path is the default.
     */
    fun isDefault() = getDefaultOutputPath() == virtualFile

    /**
     * Creates the output directory to place all produced files.
     */
    private fun createOutputPath(outPath: String): VirtualFile? {
        val mainFile = mainFile ?: return null
        if (outPath.isBlank()) return null
        val fileIndex = ProjectRootManager.getInstance(project).fileIndex

        // Create output path for non-MiKTeX systems (MiKTeX creates it automatically)
        val module = fileIndex.getModuleForFile(mainFile, false)
        if (File(outPath).mkdirs()) {
            module?.createExcludedDir(outPath)
            return LocalFileSystem.getInstance().refreshAndFindFileByPath(outPath)
        }
        return null
    }

    /**
     * Copy subdirectories of the source directory to the output directory for includes to work in non-MiKTeX systems
     */
    @Throws(ExecutionException::class)
    fun updateOutputSubDirs() {
        val includeRoot = mainFile?.parent
        val outPath = virtualFile?.path ?: return

        val files: Set<PsiFile>
        try {
            files = mainFile?.psiFile(project)?.referencedFileSet() ?: emptySet()
        }
        catch (e: IndexNotReadyException) {
            throw ExecutionException("Please wait until the indices are built.", e)
        }

        // Create output paths (see issue #70 on GitHub)
        files.asSequence()
            .mapNotNull { FileUtil.pathRelativeTo(includeRoot?.path ?: return@mapNotNull null, it.virtualFile.parent.path) }
            .forEach { File(outPath + it).mkdirs() }
    }
}