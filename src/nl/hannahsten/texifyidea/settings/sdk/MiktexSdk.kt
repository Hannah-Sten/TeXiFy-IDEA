package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand
import java.nio.file.Paths

class MiktexSdk : LatexSdk("MiKTeX SDK") {

    // Cache version
    var version: String? = null

    override fun getLatexDistributionType() = LatexDistributionType.MIKTEX

    override fun getExecutableName(executable: String, project: Project): String {
        return getExecutableName(executable, LatexSdkUtil.getLatexProjectSdk(project)?.homePath)
    }

    private fun getExecutableName(executable: String, homePath: String?): String {
        return if (LatexSdkUtil.isPdflatexInPath || homePath == null) {
            executable
        }
        else {
            val path = LatexSdkUtil.getPdflatexParentPath(Paths.get(homePath, "miktex").toString()) ?: return executable
            return Paths.get(path, executable).toString()
        }
    }

    override fun suggestHomePath(): String {
        return Paths.get(System.getProperty("user.home"), "AppData", "Local", "Programs", "MiKTeX 2.9").toString()
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        val results = mutableSetOf<String>()
        val path = "where pdflatex".runCommand()
        if (path != null) {
            val index = path.findLastAnyOf(setOf("miktex\\bin"))?.first ?: path.length - 1
            results.add(path.substring(0, index))
        }
        else {
            results.add(suggestHomePath())
        }
        return results
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
        version?.let { return version }

        val executable = getExecutableName("pdflatex", sdk.homePath)
        val output = "$executable --version".runCommand() ?: ""
        version = "\\(MiKTeX (\\d+.\\d+)\\)".toRegex().find(output)?.value

        return version
    }
}