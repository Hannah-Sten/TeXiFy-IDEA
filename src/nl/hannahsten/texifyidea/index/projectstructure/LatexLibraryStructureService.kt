package nl.hannahsten.texifyidea.index.projectstructure

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.index.file.LatexRegexBasedIndex
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.SdkPath
import nl.hannahsten.texifyidea.util.AbstractBlockingCacheService
import nl.hannahsten.texifyidea.util.CacheValueTimed
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.files.LatexPackageLocation
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.time.Duration

/**
 * Provides methods to build and manage the structure of LaTeX libraries (.sty and .cls files).
 *
 * The cache is keyed by [LibStructureCacheKey] to support different LaTeX distributions
 * in different modules. For example, `LibStructureCacheKey("/usr/local/texlive/2024", "amsmath.sty")`.
 *
 * @author Ezrnest
 */
@Service(Service.Level.PROJECT)
class LatexLibraryStructureService(
    private val project: Project
) : AbstractBlockingCacheService<LibStructureCacheKey, LatexLibraryInfo?>() {
    companion object {
        val performanceTracker = SimplePerformanceTracker()

        private val libraryCommandNameToExt: Map<String, String> = mapOf(
            "\\usepackage" to ".sty",
            "\\RequirePackage" to ".sty",
            "\\documentclass" to ".cls",
            "\\LoadClass" to ".cls"
        )

        // never expire unless invalidated manually
        private val LIBRARY_FILESET_EXPIRATION_TIME = Duration.INFINITE

        fun getInstance(project: Project): LatexLibraryStructureService = project.service()
    }

    override fun computeValue(key: LibStructureCacheKey, oldValue: LatexLibraryInfo?): LatexLibraryInfo? {
        val (sdkPath, nameWithExt) = key
        return performanceTracker.track {
            computePackageFilesetsRecur(sdkPath, nameWithExt, mutableSetOf())
        }
    }

    @Suppress("unused")
    private fun getLibraryName(current: VirtualFile, project: Project): String {
        val fileName = current.name
        if (current.fileType == StyleFileType) {
            val name = NewCommandsIndex.getByName("\\ProvidesPackage", project, current).firstNotNullOfOrNull {
                it.requiredParameterText(0)
            } ?: return fileName
            return "$name.sty"
        }
        if (current.fileType == ClassFileType) {
            val name = NewCommandsIndex.getByName("\\ProvidesClass", project, current).firstNotNullOfOrNull {
                it.requiredParameterText(0)
            } ?: return fileName
            return "$name.cls"
        }
        return fileName
    }

    private fun computePackageFilesetsRecur(sdkPath: SdkPath, nameWithExt: String, processing: MutableSet<String>): LatexLibraryInfo? {
        if (!processing.add(nameWithExt)) return null // Prevent infinite recursion
        val cacheKey = LibStructureCacheKey(sdkPath, nameWithExt)
        getUpToDateValueOrNull(cacheKey, LIBRARY_FILESET_EXPIRATION_TIME)?.let {
            return it // Return cached value if available
        }

        val path = LatexPackageLocation.getPackageLocationBySdkPath(nameWithExt, sdkPath, project) ?: run {
            Log.info("LatexLibrary not found!! $nameWithExt")
            return null
        }
        val file = LocalFileSystem.getInstance().findFileByNioFile(path) ?: return null
        val allFiles = mutableSetOf(file)
        val allPackages = mutableSetOf(nameWithExt)
        val directDependencies = mutableSetOf<String>()
        val commands = NewSpecialCommandsIndex.getPackageIncludes(project, file)
        for (command in commands) {
            val packageText = command.requiredParameterText(0) ?: continue
            val ext = libraryCommandNameToExt[command.name] ?: continue
            val refTexts = mutableListOf<String>()
            val refInfos = mutableListOf<Set<VirtualFile>>()
            for (text in packageText.split(',')) {
                val trimmed = text.trim()
                if (trimmed.isEmpty()) continue
                val name = trimmed + ext
                directDependencies.add(name)
                if (name in allPackages) continue // prevent infinite recursion
                val info = computePackageFilesetsRecur(sdkPath, name, processing)
                info?.let {
                    refTexts.add(trimmed)
                    refInfos.add(setOf(it.location))
                    allFiles.addAll(it.files)
                    allPackages.addAll(it.allIncludedPackageNames)
                }
            }
            command.putUserData(
                LatexProjectStructure.userDataKeyFileReference,
                CacheValueTimed(refTexts to refInfos)
            )
        }
        val otherPackages = LatexRegexBasedIndex.getPackageInclusions(file, project)
        otherPackages.forEach {
            val name = "$it.sty"
            directDependencies.add(name)
            if (name in allPackages) return@forEach
            val info = computePackageFilesetsRecur(sdkPath, name, processing)
            info?.let {
                allFiles.addAll(info.files)
                allPackages.addAll(info.allIncludedPackageNames)
            }
        }
        val info = LatexLibraryInfo(LatexLib.fromFileName(nameWithExt), file, allFiles, directDependencies, allPackages)
        putValue(cacheKey, info)
        Log.info("LatexLibrary Loaded: $nameWithExt (SDK: $sdkPath)")
        return info
    }

    /**
     * Get library info for a package, using the SDK resolved from the given file context.
     *
     * @param nameWithExt Package name with extension, e.g. "amsmath.sty"
     * @param contextFile The file context to determine which SDK to use
     */
    fun getLibraryInfo(nameWithExt: String, contextFile: VirtualFile?): LatexLibraryInfo? {
        val sdkPath = LatexSdkUtil.resolveSdkPath(contextFile, project) ?: return null
        return getOrComputeNow(LibStructureCacheKey(sdkPath, nameWithExt), LIBRARY_FILESET_EXPIRATION_TIME)
    }

    fun getLibraryInfo(lib: LatexLib, contextFile: VirtualFile? = null): LatexLibraryInfo? {
        val fileName = lib.toFileName() ?: return null
        return getLibraryInfo(fileName, contextFile)
    }

    fun getLibraryInfo(path: Path, contextFile: VirtualFile? = null): LatexLibraryInfo? {
        if (path.nameCount != 1) return null
        return getLibraryInfo(path.fileName.pathString, contextFile)
    }

    fun invalidateLibraryCache() {
        clearAllCache()
    }

    fun librarySize(): Int = caches.size
}