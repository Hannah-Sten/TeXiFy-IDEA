package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.TectonicSdk
import nl.hannahsten.texifyidea.util.runCommand
import java.io.File

/**
 * Cache locations of LaTeX packages in memory, because especially on Windows they can be expensive to retrieve
 * (requires a run of kpsewhich), which takes too long to do on every character typed by the user.
 * Can also be used for tex/bib files and whatever can be used with kpsewhich (probably any file that is in its search path).
 */
object LatexPackageLocationCache {

    /** Map filename with extension to full path. */
    private var cache: MutableMap<String, String?>? = null

    /**
     * Fill cache with all paths of all files in the LaTeX installation.
     * Note: this can take a long time.
     */
    fun fillCacheWithKpsewhich(project: Project) {
        // We will get all search paths that kpsewhich has, expand them and find all files
        // Source: https://www.tug.org/texinfohtml/kpathsea.html#Casefolding-search
        // We cannot just fill the cache on the fly, because then we will also run kpsewhich when the user is still typing a package name, so we will run it once for every letter typed and this is already too expensive.
        // We cannot rely on ls-R databases because they are not always populated, and running mktexlsr may run into permission issues.
        val executableName = LatexSdkUtil.getExecutableName("kpsewhich", project)
        val searchPaths = runCommand(executableName, "-show-path=tex")
        cache = runCommand(executableName, "-expand-path", searchPaths ?: ".:")?.split(File.pathSeparator)
            ?.flatMap { LocalFileSystem.getInstance().findFileByPath(it)?.children?.toList() ?: emptyList() }
            ?.filter { !it.isDirectory }
            ?.toSet()
            ?.associate { it.name to it.path }
            ?.toMutableMap() ?: mutableMapOf()
    }

    /**
     * Get the full path to the location of the package with the given name, or null in case there was any problem.
     * Note that if the package is not yet in the cache and multiple callers try to get it concurrently, then
     * the kpsewhich method will still be executed as much as there are callers.
     * If needed, this can be avoided using coroutines with a mutex (see [ReferencedFileSetCache]).
     *
     * @param name Package name with extension.
     */
    fun getPackageLocation(name: String, project: Project): String? {
        if (cache == null) {
            fillCacheWithKpsewhich(project)
        }

        // Tectonic does not have kpsewhich, but works a little differently
        val projectSdk = LatexSdkUtil.getLatexProjectSdk(project)
        val path = if (projectSdk?.sdkType is TectonicSdk) {
            (projectSdk.sdkType as TectonicSdk).getPackageLocation(name, projectSdk.homePath)
        }
        else {
            cache?.get(name)
        }
        if (path?.isBlank() == true) return null
        return path
    }
}