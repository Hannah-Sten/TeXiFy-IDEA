package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.runCommand
import java.util.Calendar

/**
 * Utility functions for WSL path conversion and availability checking.
 */
object WslPathUtil {

    /**
     * Check if WSL is available and has TeX Live installed.
     */
    val isWslTexliveAvailable: Boolean by lazy {
        if (!SystemInfo.isWindows) return@lazy false
        runCommand(*SystemEnvironment.wslCommand, "pdflatex --version")?.contains("pdfTeX") == true
    }

    /**
     * Convert a WSL path to a Windows path using wslpath.
     * e.g., /usr/local/texlive/2025 -> \\wsl.localhost\Ubuntu\usr\local\texlive\2025
     */
    fun wslPathToWindows(wslPath: String): String? = runCommand(*SystemEnvironment.wslCommand, "wslpath -w '$wslPath'")?.trim()

    /**
     * Convert a Windows path to a WSL path using wslpath.
     * e.g., \\wsl$\Ubuntu\usr\local\texlive\2025 -> /usr/local/texlive/2025
     */
    fun windowsPathToWsl(windowsPath: String): String? = runCommand(*SystemEnvironment.wslCommand, "wslpath -a '$windowsPath'")?.trim()
}

/**
 * TeX Live installation inside WSL (Windows Subsystem for Linux).
 * This SDK allows using a Linux TeX Live installation from Windows.
 *
 * The home path should be the Windows path to the WSL TeX Live installation,
 * e.g., `\\wsl$\Ubuntu\usr\local\texlive\2025` or `\\wsl.localhost\Ubuntu\usr\local\texlive\2025`
 */
class WslTexliveSdk : LatexSdk("WSL TeX Live SDK") {

    override fun suggestHomePath(): String {
        if (!SystemInfo.isWindows) return ""

        val year = Calendar.getInstance().weekYear
        return WslPathUtil.wslPathToWindows("/usr/local/texlive/$year") ?: ""
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        if (!SystemInfo.isWindows) return mutableListOf()

        val results = mutableSetOf<String>()

        // Try to find pdflatex in WSL and derive the home path
        val pdflatexPath = runCommand(*SystemEnvironment.wslCommand, "which pdflatex")?.trim()
        if (!pdflatexPath.isNullOrBlank() && pdflatexPath.contains("texlive")) {
            // Resolve symlinks
            val resolvedPath = runCommand(*SystemEnvironment.wslCommand, "readlink -f '$pdflatexPath'")?.trim()
                ?: pdflatexPath

            // Extract texlive home from path like /usr/local/texlive/2025/bin/x86_64-linux/pdflatex
            val index = resolvedPath.indexOf("/bin/")
            if (index > 0) {
                val wslHome = resolvedPath.take(index)
                WslPathUtil.wslPathToWindows(wslHome)?.let { results.add(it) }
            }
        }

        // Add default suggestion
        val defaultPath = suggestHomePath()
        if (defaultPath.isNotEmpty()) {
            results.add(defaultPath)
        }

        return results
    }

    override fun isValidSdkHome(path: String): Boolean {
        if (!SystemInfo.isWindows) return false

        // Convert Windows path to WSL path and check if pdflatex exists
        val wslPath = WslPathUtil.windowsPathToWsl(path) ?: return false
        val pdflatexCheck = runCommand(
            *SystemEnvironment.wslCommand,
            "test -x '$wslPath/bin/'*/pdflatex && echo 'found'"
        )
        return pdflatexCheck?.contains("found") == true
    }

    override fun getInvalidHomeMessage(path: String): String {
        val wslPath = WslPathUtil.windowsPathToWsl(path) ?: path
        return "Could not find $wslPath/bin/*/pdflatex in WSL"
    }

    override fun getLatexDistributionType(sdk: Sdk) = LatexDistributionType.WSL_TEXLIVE

    override fun getVersionString(sdkHome: String): String {
        val wslPath = WslPathUtil.windowsPathToWsl(sdkHome) ?: sdkHome
        val year = wslPath.split("/").lastOrNull { it.matches(Regex("\\d{4}")) } ?: "unknown"
        return "WSL TeX Live $year"
    }

    override fun getDefaultDocumentationUrl(sdk: Sdk): String? = sdk.homePath

    override fun getDefaultSourcesPath(homePath: String): VirtualFile? = LocalFileSystem.getInstance().findFileByPath("$homePath/texmf-dist/source/latex")

    override fun getDefaultStyleFilesPath(homePath: String): VirtualFile? = LocalFileSystem.getInstance().findFileByPath("$homePath/texmf-dist/tex")

    override fun getExecutableName(executable: String, homePath: String): String {
        // For WSL, we need to return the WSL path to the executable
        // The actual WSL command wrapping is done in LatexCompiler
        val wslPath = WslPathUtil.windowsPathToWsl(homePath) ?: return executable

        // Find the bin subdirectory (e.g., bin/x86_64-linux)
        val binSubdir = runCommand(
            *SystemEnvironment.wslCommand,
            "ls '$wslPath/bin' 2>/dev/null | head -1"
        )?.trim()

        return if (!binSubdir.isNullOrBlank()) {
            "$wslPath/bin/$binSubdir/$executable"
        }
        else {
            executable
        }
    }
}
