package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand
import org.jdom.Element
import java.io.File

/**
 * Represents the location of the LaTeX installation.
 *
 * @author Thomas
 */
sealed class LatexSdk(name: String) : SdkType(name) {
    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {}

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable? {
        return null
    }

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String?) = name

    override fun getPresentableName() = name

    override fun setupSdkPaths(sdk: Sdk) {
        val modificator = sdk.sdkModificator
        modificator.versionString = getVersionString(sdk)
        modificator.commitChanges() // save
    }

    // todo refactor to use sdk
    companion object {

        private val isPdflatexInPath = "pdflatex --version".runCommand()?.contains("pdfTeX") == true

        private val pdflatexVersionText: String by lazy {
            getDistribution()
        }

        private val dockerImagesText: String by lazy {
            runCommand("docker", "image", "ls") ?: ""
        }

        /**
         * Guess the LaTeX distribution that the user probably is using / wants to use.
         */
        val defaultLatexDistribution: LatexDistributionType by lazy {
            when {
                isMiktexAvailable -> LatexDistributionType.MIKTEX
                isTexliveAvailable -> LatexDistributionType.TEXLIVE
                defaultIsDockerMiktex() -> LatexDistributionType.DOCKER_MIKTEX
                else -> LatexDistributionType.TEXLIVE
            }
        }

        /**
         * Whether the user is using MikTeX or not.
         * This value is lazy, so only computed when first accessed, because it is unlikely that the user will change LaTeX distribution while using IntelliJ.
         */
        val isMiktexAvailable: Boolean by lazy {
            pdflatexVersionText.contains("MiKTeX")
        }

        /**
         * Whether the user is using TeX Live or not.
         * This value is only computed once.
         */
        val isTexliveAvailable: Boolean by lazy {
            pdflatexVersionText.contains("TeX Live")
        }

        private val isDockerMiktexAvailable: Boolean by lazy {
            dockerImagesText.contains("miktex")
        }

        private val isWslTexliveAvailable: Boolean by lazy {
            SystemInfo.isWindows && runCommand("bash", "-ic", "pdflatex --version")?.contains("pdfTeX") == true
        }

        /**
         * Whether the user does not have MiKTeX or TeX Live, but does have the miktex docker image available.
         * In this case we assume the user wants to use Dockerized MiKTeX.
         */
        private fun defaultIsDockerMiktex() = (!isMiktexAvailable && !isTexliveAvailable && dockerImagesText.contains("miktex"))

        fun isInstalled(type: LatexDistributionType): Boolean {
            if (type == LatexDistributionType.MIKTEX && isMiktexAvailable) return true
            if (type == LatexDistributionType.TEXLIVE && isTexliveAvailable) return true
            if (type == LatexDistributionType.DOCKER_MIKTEX && isDockerMiktexAvailable) return true
            if (type == LatexDistributionType.WSL_TEXLIVE && isWslTexliveAvailable) return true
            return false
        }

        /**
         * Returns year of texlive installation, 0 if it is not texlive.
         * Assumes the pdflatex version output contains something like (TeX Live 2019).
         */
        val texliveVersion: Int by lazy {
            if (!isTexliveAvailable) {
                0
            }
            else {
                val startIndex = pdflatexVersionText.indexOf("TeX Live")
                try {
                    pdflatexVersionText.substring(startIndex + "TeX Live ".length, startIndex + "TeX Live ".length + "2019".length).toInt()
                }
                catch (e: NumberFormatException) {
                    0
                }
            }
        }

        /**
         * Get the executable name of a certain LaTeX executable (e.g. pdflatex/lualatex)
         * and if needed (if not in path) prefix it with the full path to the executable using the homePath of the specified LaTeX SDK.
         *
         * @param executable Name of a program, e.g. pdflatex
         */
        fun getLatexExecutableName(executable: String, project: Project): String {
            if (isPdflatexInPath) {
                return executable
            }
            // Get base path of LaTeX distribution
            val home = ProjectRootManager.getInstance(project).projectSdk?.homePath ?: return executable
            val basePath = getPdflatexParentPath(home)
            return "$basePath/$executable"
        }

        /**
         * Given the path to the LaTeX home, find the parent path of the executables, e.g. /bin/x86_64-linux/
         */
        fun getPdflatexParentPath(homePath: String) = File("$homePath/bin").listFiles()?.firstOrNull()?.path

        /**
         * Find the full name of the distribution in use, e.g. TeX Live 2019.
         */
        private fun getDistribution(): String {
            // Could be improved by using the (project-level) LaTeX SDK if pdflatex is not in PATH
            return parsePdflatexOutput(runCommand("pdflatex", "--version") ?: "")
        }

        /**
         * Parse the output of pdflatex --version and return the distribution.
         * Assumes the distribution name is in brackets at the end of the first line.
         */
        private fun parsePdflatexOutput(output: String): String {
            val firstLine = output.split("\n")[0]
            val splitLine = firstLine.split("(", ")")

            // Get one-to-last entry, as the last one will be empty after the closing )
            return if (splitLine.size >= 2) {
                splitLine[splitLine.size - 2]
            }
            else {
                ""
            }
        }
    }
}

/**
 * TeX Live.
 */
class TexliveSdk : LatexSdk("TeX Live SDK") {

    override fun suggestHomePath(): String {
        // This method should work fast and allow running from the EDT thread.
        // It will be the starting point when someone opens the file explorer dialog to select an SDK of this type
        return "~/texlive"
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        // Note that suggested paths appear under "Detected SDK's" when adding an SDK
        val results = mutableSetOf<String>()
        val path = "which pdflatex".runCommand()
        if (path != null) {
            // Let's just assume that there is only one /bin/ in this path
            val index = path.findLastAnyOf(setOf("/bin/"))?.first ?: path.length - 1
            results.add(path.substring(0, index))
        }
        else {
            results.add("~/texlive/")
        }
        return results
    }

    override fun isValidSdkHome(path: String?): Boolean {
        if (path == null) return false

        // We expect the location of the LaTeX installation, for example ~/texlive/2020

        // If this is a valid LaTeX installation, pdflatex should be present in a subfolder in bin, e.g. $path/bin/x86_64-linux/pdflatex
        val parent = getPdflatexParentPath(path)
        return "$parent/pdflatex --version".runCommand()?.contains("pdfTeX") == true
    }

    override fun getVersionString(sdkHome: String?): String {
        return "TeX Live " + sdkHome?.split("/")?.lastOrNull { it.isNotBlank() }
    }

    override fun getDefaultDocumentationUrl(sdk: Sdk): String? {
        if (sdk.homePath == null) return null
        return sdk.homePath + ""
    }
}

class DockerSdk : LatexSdk("LaTeX Docker SDK") {

    override fun suggestHomePath(): String {
        return "/usr/bin"
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        // There's not really a path with 'sources' for docker except the location of images, but that's OS-dependent
        // and we only need to be able to execute the 'docker' executable anyway
        return "which docker".runCommand()?.let { output -> mutableListOf(
            output.split("/").dropLast(1).joinToString("/")
        ) } ?: mutableListOf()
    }

    override fun isValidSdkHome(path: String?): Boolean {
        // For now we only support miktex images
        return "$path/docker image ls".runCommand()?.contains("miktex") ?: false
    }

    override fun getVersionString(sdkHome: String?): String? {
        // todo use cached output
        // Get the tag of the first docker image with 'miktex' in the name
        val tag = "$sdkHome/docker image ls".runCommand()
            ?.split("\n")
            ?.firstOrNull { it.contains("miktex") }
            ?.split(" ")
            ?.filter { it.isNotBlank() }
            ?.get(1)
        return "Docker MiKTeX ($tag)"
    }
}