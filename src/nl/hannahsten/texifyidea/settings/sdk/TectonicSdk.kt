package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import java.io.File

/**
 * Tectonic has its own source, which is in effect TeX Live, but since it is stored not in TeX Live format
 * but in a Tectonic-specific cache it warrants its own SDK.
 */
class TectonicSdk : LatexSdk("Tectonic SDK") {
    override fun getLatexDistributionType() = LatexDistributionType.TEXLIVE

    // We assume Tectonic is in PATH.
    override fun getExecutableName(executable: String, homePath: String) = executable

    override fun suggestHomePath(): String? {
        return "~/.cache/Tectonic"
    }

    // The home path we are interested in, is actually the cache path, as it contains all the LaTeX files.
    override fun isValidSdkHome(path: String): Boolean {
        return File(path).run {
            // We are looking for a urls directory as bootstrap
            exists() && isDirectory && listFiles()?.map { it.name }?.contains("urls") == true
        }
    }

    override fun getInvalidHomeMessage(path: String) = "Please select the caches path for Tectonic"

    // todo tectonic -X bundle search

    // todo is this possible?
//    override fun getVersionString(sdkHome: String?): String? {
//        return super.getVersionString(sdkHome)
//    }

    // todo actually there are no dtx files, just sty, def, clo, cls, tec, cfg, otf, fd, ltx, enc, pfb, tex and ini?
    override fun getDefaultSourcesPath(homePath: String): VirtualFile? {
        return LocalFileSystem.getInstance().findFileByPath("$homePath/files")
    }

    override fun getDefaultStyleFilesPath(homePath: String): VirtualFile? {
        return getDefaultSourcesPath(homePath)
    }
}