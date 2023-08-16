package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand
import java.io.File

/**
 * Tectonic has its own source, which is in effect TeX Live, but since it is stored not in TeX Live format
 * but in a Tectonic-specific cache it warrants its own SDK.
 */
class TectonicSdk : LatexSdk("Tectonic SDK") {

    object Cache {
        // Map readable file name (e.g. article.sty) to actual file path on disk
        var fileLocationCache: Map<String, String>? = null
    }

    /**
     * Get the location (full path) of a package, given its name.
     * Returns empty string if the location is unknown.
     */
    fun getPackageLocation(name: String, homePath: String?): String {
        if (homePath == null) return ""

        // Avoid many threads trying to fill the cache at the same time
        synchronized(this) {
            if (Cache.fileLocationCache == null) {
                Cache.fileLocationCache = File("$homePath/urls").listFiles()
                    // Get manifest names
                    ?.mapNotNull { it.readText().trim() }
                    // Get manifest contents
                    ?.flatMap { File("$homePath/manifests/$it.txt").readLines().map { line -> line.trim() } }
                    // Example line: article.sty 1741 9697d28bf5cc3d2f...
                    ?.map { it.split(" ").filter { word -> word.isNotBlank() } }
                    ?.filter { it.size >= 2 }
                    // Map human readable file name to file name on disk
                    ?.associate { Pair(it.first(), it.last()) }
                    // Tectonic stores files like this, currently
                    ?.mapValues { "$homePath/files/${it.value.take(2)}/${it.value.drop(2)}" } ?: return ""
            }
        }
        return Cache.fileLocationCache?.get(name) ?: ""
    }

    override fun getLatexDistributionType() = LatexDistributionType.TEXLIVE

    // We assume Tectonic is in PATH.
    override fun getExecutableName(executable: String, homePath: String) = executable

    override fun suggestHomePath(): String {
        // https://github.com/tectonic-typesetting/tectonic/issues/159
        val home = System.getProperty("user.home")
        return if (SystemInfo.isMac) "$home/Library/Caches/Tectonic"
        else if (SystemInfo.isWindows) "$home/TectonicProject/Tectonic" // Did not test if this works
        else "$home/.cache/Tectonic"
    }

    // The home path we are interested in, is actually the cache path, as it contains all the LaTeX files.
    override fun isValidSdkHome(path: String): Boolean {
        return File(path).run {
            // We are looking for a urls directory as bootstrap
            exists() && isDirectory && listFiles()?.map { it.name }?.contains("urls") == true
        }
    }

    override fun getInvalidHomeMessage(path: String) = "Please select the caches path for Tectonic"

    // Actually we should return the TeX Live version, but not sure how to find it
    override fun getVersionString(sdkHome: String): String? {
        return "tectonic -V".runCommand()?.replace("Tectonic", "", ignoreCase = true)?.trim()
    }

    // Actually the dtx files are not cached, just lots of other file types.
    // sty files do seem to be present, but at the moment we're not indexing those.
    override fun getDefaultSourcesPath(homePath: String): VirtualFile? {
        return LocalFileSystem.getInstance().findFileByPath("$homePath/files")
    }

    override fun getDefaultStyleFilesPath(homePath: String): VirtualFile? {
        return getDefaultSourcesPath(homePath)
    }
}