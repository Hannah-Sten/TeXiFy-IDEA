package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.*
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.settings.LatexSdkUtil.getPdflatexParentPath
import nl.hannahsten.texifyidea.util.runCommand
import org.jdom.Element

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

    /**
     * Interface between this and [LatexDistributionType], which is used in the run configuration.
     */
    abstract fun getLatexDistributionType(): LatexDistributionType

    /**
     * If the executable (pdflatex, kpsewhich, etc.) is not in PATH, use the home path of the SDK to find it and return the full path to the executable.
     */
    abstract fun getExecutableName(executable: String, project: Project): String
}

/**
 * TeX Live.
 */
class TexliveSdk : LatexSdk("TeX Live SDK") {

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

    override fun getLatexDistributionType() = LatexDistributionType.TEXLIVE

    override fun getVersionString(sdkHome: String?): String {
        return "TeX Live " + sdkHome?.split("/")?.lastOrNull { it.isNotBlank() }
    }

    override fun getDefaultDocumentationUrl(sdk: Sdk): String? {
        if (sdk.homePath == null) return null
        return sdk.homePath + ""
    }

    /**
     * Get the executable name of a certain LaTeX executable (e.g. pdflatex/lualatex)
     * and if needed (if not in path) prefix it with the full path to the executable using the homePath of the specified LaTeX SDK.
     *
     * @param executable Name of a program, e.g. pdflatex
     */
    override fun getExecutableName(executable: String, project: Project): String {
        if (LatexSdkUtil.isPdflatexInPath) {
            return executable
        }
        // Get base path of LaTeX distribution
        val home = LatexSdkUtil.getLatexProjectSdk(project)?.homePath ?: return executable
        val basePath = getPdflatexParentPath(home)
        return "$basePath/$executable"
    }
}

class DockerSdk : LatexSdk("LaTeX Docker SDK") {

    companion object {
        val isInPath: Boolean by lazy {
            "docker --version".runCommand()?.contains("Docker version") == true
        }

        private val dockerImagesText: String by lazy {
            runCommand("docker", "image", "ls") ?: ""
        }

        val isAvailable: Boolean by lazy {
            dockerImagesText.contains("miktex")
        }
    }

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

    override fun getLatexDistributionType() = LatexDistributionType.DOCKER_MIKTEX

    override fun getVersionString(sdkHome: String?): String {
        val imagesText = if (isInPath) dockerImagesText else "$sdkHome/docker image ls".runCommand() ?: ""
        // Get the tag of the first docker image with 'miktex' in the name
        val tag = imagesText
            .split("\n")
            .firstOrNull { it.contains("miktex") }
            ?.split(" ")
            ?.filter { it.isNotBlank() }
            ?.get(1)
        return "Docker MiKTeX ($tag)"
    }

    override fun getExecutableName(executable: String, project: Project): String {
        TODO("Not yet implemented")
    }
}

class MiktexSdk : LatexSdk("MiKTeX SDK") {
    override fun getLatexDistributionType() = LatexDistributionType.MIKTEX

    override fun getExecutableName(executable: String, project: Project): String {
        TODO("Not yet implemented")
    }

    override fun suggestHomePath(): String? {
        TODO("Not yet implemented")
    }

    override fun isValidSdkHome(path: String?): Boolean {
        TODO("Not yet implemented")
    }
}