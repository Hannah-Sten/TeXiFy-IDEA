package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

/**
 * Provides stateless helpers for resolving files and names used by LaTeX run configurations.
 * These functions bridge persisted path strings and project file-system lookups.
 */
internal object LatexRunConfigurationStaticSupport {

    fun resolveMainFile(runConfig: LatexRunConfiguration, path: String? = runConfig.mainFilePath): VirtualFile? {
        val candidate = path?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val fileSystem = LocalFileSystem.getInstance()
        val absolute = fileSystem.findFileByPath(candidate) ?: fileSystem.refreshAndFindFileByPath(candidate)
        if (absolute?.extension == "tex") {
            return absolute
        }

        val contentRoots = ProjectRootManager.getInstance(runConfig.project).contentRoots
        val isAbsoluteCandidate = runCatching { Path.of(candidate).isAbsolute }.getOrDefault(false)
        if (isAbsoluteCandidate) {
            val normalizedCandidate = candidate.replace('\\', '/')
            for (contentRoot in contentRoots) {
                val normalizedRoot = contentRoot.path.replace('\\', '/')
                if (!normalizedCandidate.startsWith("$normalizedRoot/")) {
                    continue
                }
                val relativePath = normalizedCandidate.removePrefix(normalizedRoot).trimStart('/')
                val file = contentRoot.findFileByRelativePath(relativePath)
                if (file?.extension == "tex") {
                    return file
                }
            }
        }

        for (contentRoot in contentRoots) {
            val file = contentRoot.findFileByRelativePath(candidate)
            if (file?.extension == "tex") {
                return file
            }
        }
        return null
    }

    fun toProjectRelativePathOrAbsolute(runConfig: LatexRunConfiguration, file: VirtualFile): String {
        val contentRoots = ProjectRootManager.getInstance(runConfig.project).contentRoots
        val match = contentRoots
            .filter { file.path.startsWith("${it.path}/") || file.path == it.path }
            .maxByOrNull { it.path.length }
        if (match == null) {
            return file.path
        }
        return file.path.removePrefix(match.path).trimStart('/')
    }

    fun mainFileNameWithoutExtension(runConfig: LatexRunConfiguration): String? {
        val resolved = resolveMainFile(runConfig)
        if (resolved != null) {
            return resolved.nameWithoutExtension
        }
        val path = runConfig.mainFilePath ?: return null
        val slashIndex = maxOf(path.lastIndexOf('/'), path.lastIndexOf('\\'))
        val name = if (slashIndex >= 0) path.substring(slashIndex + 1) else path
        return name.substringBeforeLast('.', name)
    }
}
