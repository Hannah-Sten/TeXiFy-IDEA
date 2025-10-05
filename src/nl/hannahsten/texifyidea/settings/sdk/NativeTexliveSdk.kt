package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.util.TexifyCoroutine
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.runCommand
import nl.hannahsten.texifyidea.util.runCommandNonBlocking

/**
 * TeX Live, as installed natively by the OS's package manager.
 * Differences with [TexliveSdk] are for example that not all executables (like tlmgr) are available,
 * and the texmf-dist folder is not assumed to be in a fixed location relative to the executables, but can be anywhere.
 *
 * Documentation may or may not be present (on Arch it requires texlive-most-doc).
 * tlmgr may or may not work (on Arch it requires manual config or tllocalmgr from tllocalmgr-git).
 * Source files may or may not be present (on Arch they are present for texlive-full, but not for texlive-core).
 * All in all, it's a lot of fun.
 */
class NativeTexliveSdk : TexliveSdk("Native TeX Live SDK") {

    object Cache {
        // Path to texmf-dist, e.g. /usr/share/texmf-dist/ for texlive-core on Arch or /opt/texlive/2020/texmf-dist/ for texlive-full
        val texmfDistPath: String by lazy {
            "kpsewhich article.sty".runCommand()?.substringBefore("texmf-dist") + "texmf-dist"
        }

        var defaultStyleFilesPath: String? = null
    }

    override fun suggestHomePath(): String {
        // This method should work fast and allow running from the EDT thread.
        // It will be the starting point when someone opens the file explorer dialog to select an SDK of this type
        return "/usr/bin"
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        return TexliveSdk().suggestHomePaths().plus(suggestHomePath())
            // Avoid duplicates of TexliveSdks, which probably have x86_64-linux in the path
            .filter { path -> !path.containsAny(setOf("x86_64-linux", "universal-darwin")) }
            .toMutableSet()
    }

    override fun isValidSdkHome(path: String): Boolean {
        // We expect the location of the executables, wherever that is.
        // This is different from a TexliveSdk installation, where we have the parent directory of the TeX Live installation and find everything there.
        return LatexSdkUtil.isPdflatexPresent(path)
    }

    override fun getInvalidHomeMessage(path: String) = "Could not find $path/pdflatex"

    override fun getVersionString(sdkHome: String): String {
        // Assume pdflatex --version contains output of the form
        // pdfTeX 3.14159265-2.6-1.40.21 (TeX Live 2020/mydistro)
        val output = LatexSdkUtil.parsePdflatexOutput(runCommand("$sdkHome/pdflatex", "--version") ?: "")
        return """TeX Live (\d\d\d\d).*""".toRegex().find(output)?.value ?: "Unknown version"
    }

    override fun getDefaultDocumentationUrl(sdk: Sdk): String {
        return "${Cache.texmfDistPath}/doc"
    }

    override fun getExecutableName(executable: String, homePath: String): String {
        return "$homePath/$executable"
    }

    override fun getDefaultStyleFilesPath(homePath: String): VirtualFile? {
        // We cannot call system commands in a blocking way because it may cause freezes (#4102)
        if (Cache.defaultStyleFilesPath == null) {
            TexifyCoroutine.runInBackground {
                // Make sure to not leave it null to avoid unnecessary cache refreshes
                Cache.defaultStyleFilesPath = runCommandNonBlocking("$homePath/kpsewhich", "article.sty").output ?: ""
            }
        }

        val articlePath = Cache.defaultStyleFilesPath
        if (articlePath.isNullOrBlank()) return null

        // Assume article.sty is in tex/latex/base/article.sty
        // we go up to tex/
        return LocalFileSystem.getInstance().findFileByPath(articlePath)?.parent?.parent?.parent
    }
}