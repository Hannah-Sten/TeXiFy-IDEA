package nl.hannahsten.texifyidea.run.latex

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.util.files.createExcludedDir
import java.nio.file.Path

/**
 * Resolves output, auxiliary, and working-directory paths for LaTeX runs.
 * It expands placeholders, validates directories, and creates missing output paths when possible.
 */
internal object LatexPathResolver {

    const val PROJECT_DIR_PLACEHOLDER = "{projectDir}"
    const val MAIN_FILE_PARENT_PLACEHOLDER = "{mainFileParent}"
    private const val OUTPUT_DIR_NAME = "out"
    private const val AUX_DIR_NAME = "out"

    val defaultOutputPath: Path = Path.of(PROJECT_DIR_PLACEHOLDER, OUTPUT_DIR_NAME)
    val defaultAuxilPath: Path = Path.of(PROJECT_DIR_PLACEHOLDER, AUX_DIR_NAME)

    fun resolveOutputDir(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile? = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig),
    ): VirtualFile? =
        ensureDir(
            path = runConfig.outputPath ?: defaultOutputPath,
            defaultPath = defaultOutputPath,
            mainFile = mainFile,
            project = runConfig.project,
            variant = OUTPUT_DIR_NAME,
        )

    fun resolveAuxDir(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile? = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig),
    ): VirtualFile? {
        val hasLatexmkStep = runConfig.configOptions.steps.any { it.type == LatexStepType.LATEXMK_COMPILE }
        val supportsAuxDir = runConfig.getLatexDistributionType().isMiktex(runConfig.project, mainFile) ||
            hasLatexmkStep ||
            runConfig.hasEnabledLatexmkStep()
        if (!supportsAuxDir) {
            return null
        }
        return ensureDir(
            path = runConfig.auxilPath ?: defaultAuxilPath,
            defaultPath = defaultAuxilPath,
            mainFile = mainFile,
            project = runConfig.project,
            variant = AUX_DIR_NAME,
        )
    }

    fun resolve(path: Path?, mainFile: VirtualFile?, project: Project): Path? {
        val raw = path?.toString()?.trim().orEmpty()
        if (raw.isBlank()) return null

        val ideExpandedRaw = LatexPathMacroSupport.expandPath(raw, project, mainFile).trim().ifBlank { raw }
        val moduleRoot = getMainFileContentRoot(mainFile, project)
        val projectRootPath = moduleRoot?.path ?: mainFile?.parent?.path
        val mainFileParentPath = mainFile?.parent?.path ?: projectRootPath

        val resolvedRaw = ideExpandedRaw
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

    private fun ensureDir(
        path: Path,
        defaultPath: Path,
        mainFile: VirtualFile?,
        project: Project,
        variant: String,
    ): VirtualFile? {
        val resolvedPathString = resolve(path, mainFile, project)?.toString()
        val resolvedDefaultPathString = resolve(defaultPath, mainFile, project)?.toString()
        if (resolvedPathString == null || isInvalidJetBrainsBinPath(resolvedPathString)) {
            return fallbackDirectory(resolvedDefaultPathString, mainFile, project)
        }

        val resolvedPath = LocalFileSystem.getInstance().findFileByPath(resolvedPathString)
        if (resolvedPath != null && resolvedPath.isDirectory) {
            return resolvedPath
        }

        val created = createOutputPath(resolvedPathString, mainFile, project)
        if (created != null) {
            return created
        }

        val fallback = resolvedDefaultPathString ?: "${mainFile?.parent?.path}/$variant"
        Notification(
            "LaTeX",
            "Invalid output path",
            "Output path $resolvedPathString could not be created, trying default path $fallback",
            NotificationType.WARNING
        ).notify(project)

        return fallbackDirectory(resolvedDefaultPathString, mainFile, project)
    }

    private fun fallbackDirectory(
        defaultPath: String?,
        mainFile: VirtualFile?,
        project: Project,
    ): VirtualFile? {
        val mainFileParent = mainFile?.parent
        if (defaultPath.isNullOrBlank()) {
            return mainFileParent
        }

        LocalFileSystem.getInstance().findFileByPath(defaultPath)?.takeIf { it.isDirectory }?.let { return it }
        if (defaultPath != mainFileParent?.path) {
            createOutputPath(defaultPath, mainFile, project)?.let { return it }
            LocalFileSystem.getInstance().refreshAndFindFileByPath(defaultPath)?.takeIf { it.isDirectory }?.let { return it }
        }
        return mainFileParent
    }

    private fun createOutputPath(outPath: String, mainFile: VirtualFile?, project: Project): VirtualFile? {
        val file = mainFile ?: return null
        if (outPath.isBlank()) return null

        val module = ReadAction.compute<com.intellij.openapi.module.Module?, RuntimeException> {
            ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(file, false)
        }

        if (Path.of(outPath).toFile().mkdirs()) {
            module?.createExcludedDir(outPath)
            return LocalFileSystem.getInstance().refreshAndFindFileByPath(outPath)
        }

        return null
    }
}

internal fun isInvalidJetBrainsBinPath(path: String?): Boolean = path?.endsWith("/bin") == true
