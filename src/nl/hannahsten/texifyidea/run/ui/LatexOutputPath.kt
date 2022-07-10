package nl.hannahsten.texifyidea.run.ui

import com.intellij.ide.macro.MacroManager
import com.intellij.ide.macro.ProjectFileDirMacro
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.files.createExcludedDir
import java.io.File
import java.nio.file.Path

/**
 * Output file as a virtual file, or a promise to provide a path that can be constructed when the run configuration is actually created.
 * This allows for custom output paths in the run configuration template.
 *
 * Supports macros (see MacroManager).
 *
 * @param variant: out or auxil
 */
@Deprecated("unused")
class LatexOutputPath(private val variant: String, var contentRoot: VirtualFile?, var mainFile: VirtualFile?, private val project: Project) {

    companion object {

        // todo add main file macro
        const val mainFileString = "{mainFileParent}"
    }

    fun clone(): LatexOutputPath {
        return LatexOutputPath(variant, contentRoot, mainFile, project).apply {
            if (this@LatexOutputPath.pathString.isNotBlank()) this.pathString = this@LatexOutputPath.pathString
            this.context = this@LatexOutputPath.context
        }
    }

    // Acts as a sort of cache
    var virtualFile: VirtualFile? = null

    /** Used for resolving macros in [pathString] */
    var context: DataContext = DataContext.EMPTY_CONTEXT

    var pathString: String = "\$${ProjectFileDirMacro().name}\$/$variant"
        set(value) {
            if (value.isNotBlank()) {
                field = value
            }
        }

    /**
     * Get the output path based on the values of [virtualFile] and [pathString], create it if it does not exist.
     */
    fun getOrCreateOutputPath(): VirtualFile? {
        // No auxil directory should be present/created when there's no MiKTeX around, assuming that TeX Live does not support this
        if (!LatexSdkUtil.isMiktexAvailable && variant == "auxil") {
            return null
        }

        // Caching of the result
        return getPath(context).also {
            virtualFile = it
        }
    }

    private fun getPath(context: DataContext?): VirtualFile? {
        // When we previously made the mistake of calling findRelativePath with an empty string, the output path will be set to thee /bin folder of IntelliJ. Fix that here, to be sure
        if (virtualFile?.path?.endsWith("/bin") == true) {
            virtualFile = null
        }

        if (virtualFile != null) {
            return virtualFile!!
        }
        else {
            val pathString = MacroManager.getInstance().expandMacrosInString(pathString, true, context) ?: return null
            val path = LocalFileSystem.getInstance().findFileByPath(pathString)
            if (path != null && path.isDirectory) {
                return path
            }
            else {
                // Try to create the path
                createOutputPath(pathString)?.let { return it }
            }

            // Path is invalid (perhaps the user provided an invalid path)
            // Create and return default path
            if (contentRoot != null) {
                Notification("LaTeX", "Invalid output path", "Output path $pathString of the run configuration could not be created, trying default path ${contentRoot?.path + "/" + variant}", NotificationType.WARNING).notify(project)
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
        // Can be improved by assuming a relative path to the project, using given context
        if (outPath.isBlank() || !Path.of(outPath).isAbsolute) return null
        val fileIndex = ProjectRootManager.getInstance(project).fileIndex

        // Create output path for non-MiKTeX systems (MiKTeX creates it automatically)
        val module = fileIndex.getModuleForFile(mainFile, false)
        if (File(outPath).mkdirs()) {
            module?.createExcludedDir(outPath)
            return LocalFileSystem.getInstance().refreshAndFindFileByPath(outPath)
        }
        return null
    }
}