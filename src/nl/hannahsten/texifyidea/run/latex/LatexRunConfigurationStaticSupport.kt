package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

internal object LatexRunConfigurationStaticSupport {

    fun resolveMainFile(runConfig: LatexRunConfiguration, path: String? = runConfig.mainFilePath): VirtualFile? {
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

    fun usesAuxilOrOutDirectory(runConfig: LatexRunConfiguration): Boolean {
        val mainFile = resolveMainFile(runConfig)
        val mainParent = mainFile?.parent
        val usesAuxilDir = LatexPathResolver.resolveAuxDir(runConfig, mainFile) != mainParent
        val usesOutDir = LatexPathResolver.resolveOutputDir(runConfig, mainFile) != mainParent
        return usesAuxilDir || usesOutDir
    }
}
