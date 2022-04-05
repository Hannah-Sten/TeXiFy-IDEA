package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.TectonicSdk
import nl.hannahsten.texifyidea.util.PackageUtils.CTAN_PACKAGE_NAMES
import nl.hannahsten.texifyidea.util.runCommand
import java.io.IOException

/**
 * Cache locations of LaTeX packages in memory, because especially on Windows they can be expensive to retrieve
 * (requires a run of kpsewhich).
 * Can also be used for tex/bib files and whatever can be used with kpsewhich.
 */
object LatexPackageLocationCache {

    private val cache = mutableMapOf<String, String?>()

    /**
     * Get the full path to the location of the package with the given name, or null in case there was any problem.
     * Note that if the package is not yet in the cache and multiple callers try to get it concurrently, then
     * the kpsewhich method will still be executed as much as there are callers.
     * If needed, this can be avoided using coroutines with a mutex (see [ReferencedFileSetCache]).
     *
     * @param name Package name with extension.
     */
    fun getPackageLocation(name: String, project: Project): String? {
        // Because this method may be called on partial package names (e.g. when the user is still typing it), we first check if the package even exists before doing expensive sytem calls to find the location.
        if (name !in CTAN_PACKAGE_NAMES) return null

        if (cache.containsKey(name).not()) {
            // Tectonic does not have kpsewhich, but works a little differently
            val projectSdk = LatexSdkUtil.getLatexProjectSdk(project)
            val path = if (projectSdk?.sdkType is TectonicSdk) {
                (projectSdk.sdkType as TectonicSdk).getPackageLocation(name, projectSdk.homePath)
            }
            else {
                runKpsewhich(name, project)
            }
            cache[name] = path
            if (path?.isBlank() == true) return null
            return path
        }

        return cache[name]
    }

    private fun runKpsewhich(arg: String, project: Project): String? = try {
        val command = if (LatexSdkUtil.isMiktexAvailable) {
            // Don't install the package if not present
            "miktex-kpsewhich --miktex-disable-installer $arg"
        }
        else {
            "${LatexSdkUtil.getExecutableName("kpsewhich", project)} $arg"
        }
        command.runCommand()
    }
    catch (e: IOException) {
        null
    }
}