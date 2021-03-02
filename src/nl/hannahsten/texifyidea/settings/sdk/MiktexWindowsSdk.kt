package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
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
        if (paths != null) {
            paths.split("\r\n").forEach { path ->
                val index = path.findLastAnyOf(setOf("miktex\\bin"))?.first ?: path.length - 1
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
        } catch (ignored: InvalidPathException) {
            null
        }
    }

    override fun isValidSdkHome(path: String?): Boolean {
        // We want the MiKTeX 2.9 folder to be selected
        // Assume path is of the form C:\Users\username\AppData\Local\Programs\MiKTeX 2.9\miktex\bin\x64\pdflatex.exe
        if (path == null) {
            return false
        }
        val parent = LatexSdkUtil.getPdflatexParentPath(Paths.get(path, "miktex").toString())
        return "\"$parent\\pdflatex\" --version".runCommand()?.contains("pdfTeX") == true
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