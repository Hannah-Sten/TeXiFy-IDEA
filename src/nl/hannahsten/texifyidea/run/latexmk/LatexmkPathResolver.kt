package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Files
import java.nio.file.Path

object LatexmkPathResolver {

    const val PROJECT_DIR_PLACEHOLDER = "{projectDir}"
    const val MAIN_FILE_PARENT_PLACEHOLDER = "{mainFileParent}"

    data class ResolvedDirs(
        val outputDir: Path,
        val auxilDir: Path?,
        val shouldPassAuxilDir: Boolean,
    )

    fun resolveOutputDir(runConfig: LatexmkRunConfiguration): Path? {
        val mainParent = runConfig.mainFileParentPath() ?: return null
        val raw = runConfig.outputPathRaw.ifBlank { MAIN_FILE_PARENT_PLACEHOLDER }
        return resolvePath(raw, runConfig) ?: mainParent
    }

    fun resolveAuxDir(runConfig: LatexmkRunConfiguration): Path? {
        val raw = runConfig.auxilPathRaw.ifBlank { return null }
        return resolvePath(raw, runConfig)
    }

    fun resolveOutAuxPair(runConfig: LatexmkRunConfiguration): ResolvedDirs? {
        val outputDir = resolveOutputDir(runConfig) ?: return null
        val auxilDir = resolveAuxDir(runConfig)
        return ResolvedDirs(
            outputDir = outputDir,
            auxilDir = auxilDir,
            shouldPassAuxilDir = auxilDir != null && auxilDir != outputDir,
        )
    }

    fun ensureDirectories(runConfig: LatexmkRunConfiguration): ResolvedDirs? {
        val resolved = resolveOutAuxPair(runConfig) ?: return null
        runCatching { Files.createDirectories(resolved.outputDir) }
        resolved.auxilDir?.let { runCatching { Files.createDirectories(it) } }
        return resolved
    }

    fun toVirtualFile(path: Path?): VirtualFile? {
        if (path == null) return null
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path.toString())
    }

    private fun resolvePath(rawPath: String, runConfig: LatexmkRunConfiguration): Path? {
        val mainParent = runConfig.mainFileParentPath() ?: return null
        val projectDir = runConfig.mainFileContentRootPath() ?: runConfig.project.basePath ?: mainParent.toString()

        val replaced = rawPath
            .replace(MAIN_FILE_PARENT_PLACEHOLDER, mainParent.toString())
            .replace(PROJECT_DIR_PLACEHOLDER, projectDir)

        val parsed = runCatching { Path.of(replaced) }.getOrNull() ?: return null
        return if (parsed.isAbsolute) {
            parsed.normalize()
        }
        else {
            (runConfig.getResolvedWorkingDirectory() ?: mainParent).resolve(parsed).normalize()
        }
    }

    private fun LatexmkRunConfiguration.mainFileParentPath(): Path? = mainFile?.parent?.path?.let { Path.of(it) }

    private fun LatexmkRunConfiguration.mainFileContentRootPath(): String? {
        val file = mainFile ?: return null
        return runReadAction {
            ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(file)?.path
        }
    }
}
