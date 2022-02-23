package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand
import java.nio.file.InvalidPathException
import java.nio.file.Paths

/**
 * MiKTeX on Windows.
 */
class MiktexWindowsSdk : LatexSdk("MiKTeX Windows SDK") {

    companion object {

        // Cache version
        var version: String? = null
    }

    override fun getLatexDistributionType() = LatexDistributionType.MIKTEX

    override fun getExecutableName(executable: String, homePath: String): String {
        val path = LatexSdkUtil.getPdflatexParentPath(Paths.get(homePath, "miktex").toString()) ?: return executable
        return Paths.get(path, executable).toString()
    }

    override fun suggestHomePath(): String {
        return Paths.get(System.getProperty("user.home"), "AppData", "Local", "Programs", "MiKTeX 2.9").toString()
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        val results = mutableSetOf<String>()
        val paths = "where pdflatex".runCommand()
        if (paths != null && !paths.contains("Could not find")) { // Full output is INFO: Could not find files for the given pattern(s).
            paths.split("\r\n").forEach { path ->
                val index = path.findLastAnyOf(setOf("miktex\\bin"))?.first ?: (path.length - 1)
                results.add(path.substring(0, index))
            }
        }
        else {
            results.add(suggestHomePath())
        }
        return results
    }

    override fun getDefaultSourcesPath(homePath: String): VirtualFile? {
        return try {
            // To save space, MiKTeX leaves source/latex empty by default, but does leave the zipped files in source/
            LocalFileSystem.getInstance().findFileByPath(Paths.get(homePath, "source").toString())
        }
        catch (ignored: InvalidPathException) {
            null
        }
    }

    override fun isValidSdkHome(path: String): Boolean {
        // We want the MiKTeX 2.9 folder to be selected
        // Assume path is of the form C:\Users\username\AppData\Local\Programs\MiKTeX 2.9\miktex\bin\x64\pdflatex.exe
        val directory = LatexSdkUtil.getPdflatexParentPath(Paths.get(path, "miktex").toString())
        val errorMessage = "Could not find $path/miktex/bin/*/pdflatex, please make sure you selected the MiKTeX installation directory."
        return LatexSdkUtil.isPdflatexPresent(directory, errorMessage, name, suppressNotification = suggestHomePaths().plus(suggestHomePath()))
    }

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
}