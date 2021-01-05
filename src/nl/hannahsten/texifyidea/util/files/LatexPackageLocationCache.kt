package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

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
        if (cache.containsKey(name).not()) {
            val path = runKpsewhich(name, project)
            cache[name] = path
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
        BufferedReader(
                InputStreamReader(Runtime.getRuntime().exec(command).inputStream)
        ).readLine() // Returns null if no line read.
    }
    catch (e: IOException) {
        null
    }
}