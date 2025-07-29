package nl.hannahsten.texifyidea.util.files

import arrow.atomic.AtomicInt
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.index.pathOrNull
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.TectonicSdk
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.CacheService
import nl.hannahsten.texifyidea.util.TexifyProjectCacheService
import nl.hannahsten.texifyidea.util.isTestProject
import nl.hannahsten.texifyidea.util.runCommand
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/**
 * Cache locations of LaTeX packages in memory, because especially on Windows they can be expensive to retrieve
 * (requires a run of kpsewhich), which takes too long to do on every character typed by the user.
 * Can also be used for tex/bib files and whatever can be used with kpsewhich (probably any file that is in its search path).
 */
object LatexPackageLocation {

    const val EXPIRATION_TIME = 1 * 60 * 60 * 1000L // 1 hour, or any other time that is reasonable to keep the cache around

    var retries = AtomicInt(0)

    val cacheKey = CacheService.createKey<Map<String, Path>>()

    /**
     * Fill cache with all paths of all files in the LaTeX installation.
     * Note: this can take a long time.
     */
    suspend fun updateLocationWithKpsewhichSuspend(project: Project) {
        TexifyProjectCacheService.getInstance(project).computeOrSkip(cacheKey, ::computeLocationWithKpsewhich)
    }

    /**
     * Fill cache with all paths of all files in the LaTeX installation.
     * Note: this can take a long time.
     */
    fun computeLocationWithKpsewhich(project: Project): Map<String, Path> {
        /** Map filename with extension to full path. */
        if(project.isTestProject()) return emptyMap()
        // We will get all search paths that kpsewhich has, expand them and find all files
        // Source: https://www.tug.org/texinfohtml/kpathsea.html#Casefolding-search
        // We cannot just fill the cache on the fly, because then we will also run kpsewhich when the user is still typing a package name, so we will run it once for every letter typed and this is already too expensive.
        // We cannot rely on ls-R databases because they are not always populated, and running mktexlsr may run into permission issues.
        val executableName = LatexSdkUtil.getExecutableName("kpsewhich", project)
        val texPaths = runCommand(executableName, "article.cls", "plain.bst", timeout = 10)
        // See NativeTexliveSdk.getDefaultStyleFilesPath which does the same thing, but for a specific file
        // This ensures that the library root folders are consistent

        if (retries.getAndIncrement() <= 5 && texPaths.isNullOrBlank()) {
            // I think this should always return something, so if it doesn't we assume something went wrong and we need to try again later
            return emptyMap()
        }
        if (texPaths == null) return emptyMap()
        // this should be like
        // /usr/local/texlive/2025/texmf-dist/tex/latex/base/article.cls
        // /usr/local/texlive/2023/texmf-dist/bibtex/bst/base/plain.bst
        val rootFolders = texPaths.lines().mapNotNull { pathOrNull(it)?.parent?.parent?.parent }
        // /usr/local/texlive/2025/texmf-dist/tex and /usr/local/texlive/2023/texmf-dist/bibtex/bst/
        // search all the subdirectories of the root folders
        val result = mutableMapOf<String, Path>()
        rootFolders.forEach { root ->
            root.toFile().walk().map { it.toPath() }.filter { it.isRegularFile() }
                .forEach {
                    val fileName = it.fileName.toString()
                    // If the file is already in the map, we keep the first one we found
                    result.putIfAbsent(fileName, it)
                }
        }
        Log.debug("Latex package location cache generated with ${result.size} paths")
        return result
    }

    /**
     * Get the full path to the location of the package with the given name, or null in case there was any problem.
     * Note that if the package is not yet in the cache and multiple callers try to get it concurrently, then
     * the kpsewhich method will still be executed as much as there are callers.
     * If needed, this can be avoided using coroutines with a mutex (see [TexifyProjectCacheService]).
     *
     * @param name Package name with extension.
     */
    fun getPackageLocation(name: String, project: Project): Path? {
        if(ApplicationManager.getApplication().isUnitTestMode) {
            return null
        }
        // Tectonic does not have kpsewhich, but works a little differently
        val projectSdk = LatexSdkUtil.getLatexProjectSdk(project)
        val path = if (projectSdk?.sdkType is TectonicSdk) {
            pathOrNull((projectSdk.sdkType as TectonicSdk).getPackageLocation(name, projectSdk.homePath))
        }
        else {
            val cache = TexifyProjectCacheService.getInstance(project).getAndComputeLater(
                cacheKey, EXPIRATION_TIME, ::computeLocationWithKpsewhich
            )
            cache?.get(name)
        }
        return path
    }
}