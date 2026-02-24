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

    val defaultOutputPath: Path = Path.of(MAIN_FILE_PARENT_PLACEHOLDER)
    val defaultAuxilPath: Path = Path.of(MAIN_FILE_PARENT_PLACEHOLDER)

    fun resolveOutputDir(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile? = runConfig.executionState.resolvedMainFile,
    ): VirtualFile? =
        ensureDir(runConfig.outputPath ?: defaultOutputPath, mainFile, runConfig.project, "out")

    fun resolveAuxDir(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile? = runConfig.executionState.resolvedMainFile,
    ): VirtualFile? {
        val hasLatexmkStep = runConfig.configOptions.steps.any { it.enabled && it.type == LatexStepType.LATEXMK_COMPILE }
        val supportsAuxDir = runConfig.getLatexDistributionType().isMiktex(runConfig.project, mainFile) ||
            hasLatexmkStep ||
            runConfig.hasEnabledLatexmkStep()
        if (!supportsAuxDir) {
            return null
        }
        return ensureDir(runConfig.auxilPath ?: defaultAuxilPath, mainFile, runConfig.project, "auxil")
    }

    fun resolve(path: Path?, mainFile: VirtualFile?, project: Project): Path? {
        val raw = path?.toString()?.trim().orEmpty()
        if (raw.isBlank()) return null

        val moduleRoot = getMainFileContentRoot(mainFile, project)
        val projectRootPath = moduleRoot?.path ?: mainFile?.parent?.path
        val mainFileParentPath = mainFile?.parent?.path ?: projectRootPath

        val resolvedRaw = raw
            .replace(PROJECT_DIR_PLACEHOLDER, projectRootPath ?: return null)
            .replace(MAIN_FILE_PARENT_PLACEHOLDER, mainFileParentPath ?: return null)
            .takeIf { it.isNotBlank() }
            ?: return null

        val resolved = pathOrNull(resolvedRaw)

        if (resolved?.isAbsolute == true) {
            return resolved
        }

        return resolveRelativePathAgainstContentRoots(resolvedRaw, moduleRoot, project) ?: resolved
    }

    fun getMainFileContentRoot(mainFile: VirtualFile?, project: Project): VirtualFile? {
        if (mainFile == null || !project.isInitialized) return null
        return ReadAction.compute<VirtualFile?, RuntimeException> {
            ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile)
        }
    }

    private fun resolveRelativePathAgainstContentRoots(path: String, moduleRoot: VirtualFile?, project: Project): Path? {
        if (moduleRoot != null) {
            moduleRoot.findFileByRelativePath(path)?.let { return Path.of(it.path) }
            return Path.of(moduleRoot.path).resolve(path).normalize()
        }

        return runCatching {
            ReadAction.compute<Path?, RuntimeException> {
                ProjectRootManager.getInstance(project).contentRoots.firstNotNullOfOrNull { root ->
                    root.findFileByRelativePath(path)?.let { Path.of(it.path) }
                }
            }
        }.getOrNull()
    }

    @Throws(ExecutionException::class)
    fun updateOutputSubDirs(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile? = runConfig.executionState.resolvedMainFile,
        outputDir: VirtualFile? = resolveOutputDir(runConfig, mainFile),
    ): Set<File> {
        val includeRoot = mainFile?.parent ?: return emptySet()
        val outPath = outputDir?.path ?: return emptySet()
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
        val resolvedPathString = resolve(path, mainFile, project)?.toString()
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

internal fun isInvalidJetBrainsBinPath(path: String?): Boolean = path?.endsWith("/bin") == true
