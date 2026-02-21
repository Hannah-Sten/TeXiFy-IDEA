package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.util.files.FileUtil
import nl.hannahsten.texifyidea.util.files.allChildDirectories
import nl.hannahsten.texifyidea.util.files.createExcludedDir
import java.io.File
import java.nio.file.Path

internal object LatexPathResolver {

    const val PROJECT_DIR_PLACEHOLDER = "{projectDir}"
    const val MAIN_FILE_PARENT_PLACEHOLDER = "{mainFileParent}"

    private val defaultOutputPath: Path = Path.of("$PROJECT_DIR_PLACEHOLDER/out")
    private val defaultAuxilPath: Path = Path.of("$PROJECT_DIR_PLACEHOLDER/auxil")

    fun resolveOutputDir(runConfig: LatexRunConfiguration): VirtualFile? = ensureDir(runConfig.outputPath ?: defaultOutputPath, runConfig.mainFile, runConfig.project, "out")

    fun resolveAuxDir(runConfig: LatexRunConfiguration): VirtualFile? {
        if (!runConfig.getLatexDistributionType().isMiktex(runConfig.project, runConfig.mainFile)) {
            return null
        }
        return ensureDir(runConfig.auxilPath ?: defaultAuxilPath, runConfig.mainFile, runConfig.project, "auxil")
    }

    fun resolve(path: Path?, mainFile: VirtualFile?, project: Project): Path? {
        val pathString = resolveToString(path, mainFile, project) ?: return null
        return pathOrNull(pathString)
    }

    fun resolveToString(path: Path?, mainFile: VirtualFile?, project: Project): String? {
        val raw = path?.toString()?.trim().orEmpty()
        if (raw.isBlank()) return null

        val moduleRoot = getMainFileContentRoot(mainFile, project)
        val projectRootPath = moduleRoot?.path ?: mainFile?.parent?.path
        val mainFileParentPath = mainFile?.parent?.path ?: projectRootPath

        return raw
            .replace(PROJECT_DIR_PLACEHOLDER, projectRootPath ?: return null)
            .replace(MAIN_FILE_PARENT_PLACEHOLDER, mainFileParentPath ?: return null)
            .takeIf { it.isNotBlank() }
    }

    fun getMainFileContentRoot(mainFile: VirtualFile?, project: Project): VirtualFile? {
        if (mainFile == null || !project.isInitialized) return null
        return ReadAction.compute<VirtualFile?, RuntimeException> {
            ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile)
        }
    }

    @Throws(ExecutionException::class)
    fun updateOutputSubDirs(runConfig: LatexRunConfiguration): Set<File> {
        val includeRoot = runConfig.mainFile?.parent ?: return emptySet()
        val outPath = resolveOutputDir(runConfig)?.path ?: return emptySet()
        val files: Set<VirtualFile> = includeRoot.allChildDirectories()
        val createdDirectories = mutableSetOf<File>()

        files.asSequence()
            .filter { !it.path.contains(outPath) }
            .mapNotNull { FileUtil.pathRelativeTo(includeRoot.path, it.path) }
            .forEach {
                val file = File(outPath, it)
                if (file.mkdirs()) {
                    createdDirectories.add(file)
                }
            }

        return createdDirectories
    }

    private fun ensureDir(path: Path, mainFile: VirtualFile?, project: Project, variant: String): VirtualFile? {
        val resolvedPathString = resolveToString(path, mainFile, project)
        if (resolvedPathString == null || isInvalidJetBrainsBinPath(resolvedPathString)) {
            return mainFile?.parent
        }

        val resolvedPath = LocalFileSystem.getInstance().findFileByPath(resolvedPathString)
        if (resolvedPath != null && resolvedPath.isDirectory) {
            return resolvedPath
        }

        val created = createOutputPath(resolvedPathString, mainFile, project)
        if (created != null) {
            return created
        }

        val fallback = getMainFileContentRoot(mainFile, project)
        Notification(
            "LaTeX",
            "Invalid output path",
            "Output path $resolvedPathString could not be created, trying default path ${fallback?.path}/$variant",
            NotificationType.WARNING
        ).notify(project)

        if (fallback != null) {
            val defaultPath = fallback.path + "/" + variant
            createOutputPath(defaultPath, mainFile, project)?.let { return it }
            return fallback
        }

        return mainFile?.parent
    }

    private fun createOutputPath(outPath: String, mainFile: VirtualFile?, project: Project): VirtualFile? {
        val file = mainFile ?: return null
        if (outPath.isBlank()) return null

        val module = ReadAction.compute<com.intellij.openapi.module.Module?, RuntimeException> {
            ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(file, false)
        }

        if (File(outPath).mkdirs()) {
            module?.createExcludedDir(outPath)
            return LocalFileSystem.getInstance().refreshAndFindFileByPath(outPath)
        }

        return null
    }
}
