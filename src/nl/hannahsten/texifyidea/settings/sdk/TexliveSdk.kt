package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand

/**
 * TeX Live, as installed in the recommended way by https://www.tug.org/texlive/quickinstall.html
 */
open class TexliveSdk(name: String = "TeX Live SDK") : LatexSdk(name) {

    companion object {

        /**
         * Returns year of texlive installation, 0 if it is not texlive.
         * Assumes the pdflatex version output contains something like (TeX Live 2019).
         */
        val version: Int by lazy {
            if (!isAvailable) {
                0
            }
            else {
                val startIndex = LatexSdkUtil.pdflatexVersionText.indexOf("TeX Live")
                try {
                    LatexSdkUtil.pdflatexVersionText.substring(
                        startIndex + "TeX Live ".length,
                        startIndex + "TeX Live ".length + "2019".length
                    ).toInt()
                }
                catch (e: NumberFormatException) {
                    0
                }
            }
        }

        /**
         * Whether the user is using TeX Live or not.
         * This value is only computed once.
         */
        val isAvailable: Boolean by lazy {
            LatexSdkUtil.pdflatexVersionText.contains("TeX Live")
        }
    }

    override fun suggestHomePath(): String {
        // This method should work fast and allow running from the EDT thread.
        // It will be the starting point when someone opens the file explorer dialog to select an SDK of this type
        return "~/texlive"
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        // Note that suggested paths appear under "Detected SDK's" when adding an SDK
        val results = mutableSetOf<String>()
        val path = "which pdflatex".runCommand()
        if (!path.isNullOrEmpty()) {
            // Let's just assume that there is only one /bin/ in this path
            val index = path.findLastAnyOf(setOf("/bin/"))?.first ?: path.length - 1
            results.add(path.substring(0, index))
        }
        else {
            results.add(suggestHomePath())
        }
        return results
    }

    override fun isValidSdkHome(path: String?): Boolean {
        if (path == null) return false

        // We expect the location of the LaTeX installation, for example ~/texlive/2020

        // If this is a valid LaTeX installation, pdflatex should be present in a subfolder in bin, e.g. $path/bin/x86_64-linux/pdflatex
        val parent = LatexSdkUtil.getPdflatexParentPath(path)
        return "$parent/pdflatex --version".runCommand()?.contains("pdfTeX") == true
    }

    override fun getLatexDistributionType() = LatexDistributionType.TEXLIVE

    override fun getVersionString(sdkHome: String?): String {
        return "TeX Live " + sdkHome?.split("/")?.lastOrNull { it.isNotBlank() }
    }

    override fun getDefaultDocumentationUrl(sdk: Sdk): String? {
        if (sdk.homePath == null) return null
        return sdk.homePath
    }

    override fun getDefaultSourcesPath(homePath: String): VirtualFile? {
        return LocalFileSystem.getInstance().findFileByPath("$homePath/texmf-dist/source/latex")
    }

    override fun getDefaultStyleFilesPath(homePath: String): VirtualFile? {
        return LocalFileSystem.getInstance().findFileByPath("$homePath/texmf-dist/tex/latex")
    }

    override fun getExecutableName(executable: String, homePath: String): String {
        // Get base path of LaTeX distribution
        val basePath = LatexSdkUtil.getPdflatexParentPath(homePath)
        return "$basePath/$executable"
    }
}