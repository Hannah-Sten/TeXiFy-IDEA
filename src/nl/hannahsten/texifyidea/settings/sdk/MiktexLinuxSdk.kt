package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand
import java.nio.file.Paths

/**
 * MiKTeX on Linux has a very different file structure than MiKTeX on Windows, so this warrants its own SDK type.
 * This has been tested on Arch Linux, and let's hope it's similar for other distros.
 * Based on user feedback, I'm guessing that it is very similar to MiKTeX for Mac.
 *
 * On Arch (user install), the pdflatex binary was created in ~/bin and MiKTeX itself was installed to /opt/miktex, the texmf folder was in ~/.miktex.
 */
class MiktexLinuxSdk : LatexSdk("MiKTeX Mac/Linux SDK") {

    companion object {

        // Cache version
        var version: String? = null
    }

    override fun getLatexDistributionType() = LatexDistributionType.MIKTEX

    override fun getExecutableName(executable: String, homePath: String): String {
        return "$homePath/$executable"
    }

    override fun suggestHomePath(): String {
        // The user can by default install globally or for the user only (see below)
        // Similar as with Windows we recommend installing for the user only.
        return "~/bin/"
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        return listOf(
            Paths.get(System.getProperty("user.home"), "bin").toString(),
            "/usr/local/bin"
        ).toMutableList()
    }

    override fun isValidSdkHome(path: String): Boolean {
        // We just want a path where pdflatex is present
        return LatexSdkUtil.isPdflatexPresent(path)
    }

    override fun getInvalidHomeMessage(path: String) = "Could not find $path/pdflatex"

    override fun getVersionString(sdk: Sdk): String? {
        return getVersionString(sdk.homePath)
    }

    override fun getVersionString(sdkHome: String?): String? {
        version?.let { return version }

        val executable = sdkHome?.let { getExecutableName("pdflatex", it) } ?: "pdflatex"
        val output = "$executable --version".runCommand() ?: ""
        version = "\\(MiKTeX (\\d+.\\d+)\\)".toRegex().find(output)?.value

        return version
    }

    override fun getDefaultSourcesPath(homePath: String): VirtualFile? {
        // This was the path on the tested Arch installation
        // Note that also these files are zipped
        return LocalFileSystem.getInstance().findFileByPath(Paths.get(System.getProperty("user.home"), ".miktex", "texmfs", "install", "source").toString())
    }

    override fun getDefaultStyleFilesPath(homePath: String): VirtualFile? {
        return LocalFileSystem.getInstance().findFileByPath(Paths.get(System.getProperty("user.home"), ".miktex", "texmfs", "install", "tex", "latex").toString())
    }
}