package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull

internal object LatexRunConfigurationStaticSupport {

    fun resolveMainFile(runConfig: LatexRunConfiguration, path: String? = runConfig.mainFilePath): VirtualFile? {
        if (path == null || path == runConfig.mainFilePath) {
            runConfig.executionState.resolvedMainFile?.let { return it }
            if (runConfig.executionState.isInitialized) {
                return null
            }
        }

        val candidate = path?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val fileSystem = LocalFileSystem.getInstance()
        val absolute = fileSystem.findFileByPath(candidate)
        if (absolute?.extension == "tex") {
            return absolute
        }

        val contentRoots = ProjectRootManager.getInstance(runConfig.project).contentRoots
        for (contentRoot in contentRoots) {
            val file = contentRoot.findFileByRelativePath(candidate)
            if (file?.extension == "tex") {
                return file
            }
        }
        return null
    }

    fun normalizeMainFilePath(runConfig: LatexRunConfiguration, path: String?): String? {
        val trimmed = path?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val resolved = resolveMainFile(runConfig, trimmed)
        return resolved?.let { toProjectRelativePathOrAbsolute(runConfig, it) } ?: trimmed
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
        val resolved = if (runConfig.executionState.isInitialized) runConfig.executionState.resolvedMainFile else resolveMainFile(runConfig)
        if (resolved != null) {
            return resolved.nameWithoutExtension
        }
        val path = runConfig.mainFilePath ?: return null
        val slashIndex = maxOf(path.lastIndexOf('/'), path.lastIndexOf('\\'))
        val name = if (slashIndex >= 0) path.substring(slashIndex + 1) else path
        return name.substringBeforeLast('.', name)
    }

    fun usesAuxilOrOutDirectory(runConfig: LatexRunConfiguration): Boolean {
        if (runConfig.executionState.isInitialized) {
            val mainParent = runConfig.executionState.resolvedMainFile?.parent
            val usesAuxilDir = (runConfig.executionState.resolvedAuxDir ?: runConfig.executionState.resolvedOutputDir) != mainParent
            val usesOutDir = runConfig.executionState.resolvedOutputDir != mainParent
            return usesAuxilDir || usesOutDir
        }

        val mainFile = resolveMainFile(runConfig)
        val mainParent = mainFile?.parent
        val usesAuxilDir = LatexPathResolver.resolveAuxDir(runConfig, mainFile) != mainParent
        val usesOutDir = LatexPathResolver.resolveOutputDir(runConfig, mainFile) != mainParent
        return usesAuxilDir || usesOutDir
    }

    fun applyLegacyOutAuxFlags(runConfig: LatexRunConfiguration, auxDirBoolean: String?, outDirBoolean: String?) {
        val main = resolveMainFile(runConfig) ?: return
        if (auxDirBoolean != null && runConfig.auxilPath == null) {
            val usesAuxDir = java.lang.Boolean.parseBoolean(auxDirBoolean)
            val moduleRoot = ProjectRootManager.getInstance(runConfig.project).fileIndex.getContentRootForFile(main)
            val path = if (usesAuxDir) moduleRoot?.path + "/auxil" else main.parent.path
            runConfig.auxilPath = pathOrNull(path)
        }
        if (outDirBoolean != null && runConfig.outputPath == null) {
            val usesOutDir = java.lang.Boolean.parseBoolean(outDirBoolean)
            val moduleRoot = ProjectRootManager.getInstance(runConfig.project).fileIndex.getContentRootForFile(main)
            val path = if (usesOutDir) moduleRoot?.path + "/out" else main.parent.path
            runConfig.outputPath = pathOrNull(path)
        }
    }
}
