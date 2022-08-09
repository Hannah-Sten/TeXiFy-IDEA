package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.runCommand
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.nio.file.InvalidPathException
import java.nio.file.Paths

/**
 * MiKTeX on Windows.
 */
class MiktexWindowsSdk : LatexSdk("MiKTeX Windows SDK") {

    companion object {

        // Cache version
        var version: DefaultArtifactVersion? = null
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
                if (path.isBlank()) return@forEach
                val index = path.findLastAnyOf(setOf("miktex\\bin"))?.first ?: (path.length - 1)
                if (index > 0) {
                    results.add(path.substring(0, index))
                }
            }
        }
        else {
            results.add(suggestHomePath())
        }
        return results
    }

    override fun getDefaultSourcesPath(homePath: String): VirtualFile? {
        val path = Paths.get(homePath, "source").toString()
        return try {
            // To save space, MiKTeX leaves source/latex empty by default, but does leave the zipped files in source/
            LocalFileSystem.getInstance().findFileByPath(path)
        }
        catch (ignored: InvalidPathException) {
            Log.debug("Invalid path $path when looking for LaTeX sources")
            null
        }
    }

    override fun getDefaultStyleFilesPath(homePath: String): VirtualFile? {
        val path = Paths.get(homePath, "tex", "latex").toString()
        return try {
            LocalFileSystem.getInstance().findFileByPath(path)
        }
        catch (ignored: InvalidPathException) {
            Log.debug("Invalid path $path when looking for LaTeX style files")
            null
        }
    }

    override fun isValidSdkHome(path: String): Boolean {
        // We want the MiKTeX 2.9 folder to be selected
        // Assume path is of the form C:\Users\username\AppData\Local\Programs\MiKTeX 2.9\miktex\bin\x64\pdflatex.exe
        val directory = LatexSdkUtil.getPdflatexParentPath(Paths.get(path, "miktex").toString())
        return LatexSdkUtil.isPdflatexPresent(directory)
    }

    override fun getInvalidHomeMessage(path: String) = "Could not find $path/miktex/bin/*/pdflatex, please make sure you selected the MiKTeX installation directory."

    override fun getVersionString(sdk: Sdk): String {
        return getVersionString(sdk.homePath)
    }

    override fun getVersionString(sdkHome: String?) = "MiKTeX " + getVersion(sdkHome).toString()

    fun getVersion(sdkHome: String?): DefaultArtifactVersion {
        version?.let { return it }
        val executable = sdkHome?.let { getExecutableName("pdflatex", it) } ?: "pdflatex"
        val output = "$executable --version".runCommand() ?: ""
        val versionString = "\\(MiKTeX (\\d+.\\d+)\\)".toRegex().find(output)?.groups?.get(1)?.value ?: ""
        version = DefaultArtifactVersion(versionString)
        return version!!
    }
}