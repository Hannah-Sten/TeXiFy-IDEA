package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand
import java.io.File
import java.util.*

/**
 * TeX Live, as installed in the recommended way by https://www.tug.org/texlive/quickinstall.html (Windows or Linux).
 */
open class TexliveSdk(name: String = "TeX Live SDK") : LatexSdk(name) {

    object Cache {
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
        // Guess defaults without accessing file system
        val year = Calendar.getInstance().weekYear
        return if (SystemInfo.isMac) "/usr/local/texlive/$year" else "~/texlive/$year"
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        // Note that suggested paths appear under "Detected SDK's" when adding an SDK
        val results = mutableSetOf(suggestHomePath())
        val paths = if (SystemInfo.isWindows) "where pdflatex".runCommand() else "which pdflatex".runCommand()
        if (!paths.isNullOrEmpty()) {
            for (path in paths.split("\\s+".toRegex())) {
                // Resolve symlinks
                val resolvedPath = if (!SystemInfo.isWindows) runCommand("readlink", "-f", path) ?: path else path

                // We don't know for sure whether this path contains 'texlive':
                // e.g. C:\texnolive\2021\bin\pdflatex.exe can be perfectly valid
                if (resolvedPath.contains("miktex", ignoreCase = true)) {
                    continue
                }

                // Let's just assume that there is only one /bin/ in this path
                val index = resolvedPath.findLastAnyOf(setOf(File.separator + "bin" + File.separator))?.first ?: (resolvedPath.length - 1)
                if (index > 0) {
                    results.add(resolvedPath.substring(0, index))
                }
            }
        }

        return results
    }

    override fun isValidSdkHome(path: String): Boolean {
        // We expect the location of the LaTeX installation, for example ~/texlive/2020

        // If this is a valid LaTeX installation, pdflatex should be present in a subfolder in bin, e.g. $path/bin/x86_64-linux/pdflatex
        val parent = LatexSdkUtil.getPdflatexParentPath(path)
        return LatexSdkUtil.isPdflatexPresent(parent)
    }

    override fun getInvalidHomeMessage(path: String) = "Could not find $path/bin/*/pdflatex"

    override fun getLatexDistributionType(sdk: Sdk) = LatexDistributionType.TEXLIVE

    override fun getVersionString(sdkHome: String): String {
        return "TeX Live " + sdkHome.split("/").lastOrNull { it.isNotBlank() }
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
        return if (basePath != null) "$basePath/$executable" else executable
    }
}