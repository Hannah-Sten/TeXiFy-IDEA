package nl.hannahsten.texifyidea.settings.sdk

import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand

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

    override fun getExecutableName(executable: String, homePath: String): String {
        // Could be improved by prefixing docker command here, but needs to be in sync with LatexCompiler
        return executable
    }
}