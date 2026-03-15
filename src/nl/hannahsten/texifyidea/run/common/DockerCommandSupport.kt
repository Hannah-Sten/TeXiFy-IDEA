package nl.hannahsten.texifyidea.run.common

import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.settings.sdk.DockerSdk
import nl.hannahsten.texifyidea.settings.sdk.DockerSdkAdditionalData
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.runCommand

internal object DockerCommandSupport {

    fun prependDockerRunCommand(
        session: LatexRunSessionState,
        command: MutableList<String>,
        dockerOutputDir: String?,
        dockerAuxDir: String?,
    ) {
        val isMiktex = session.distributionType == LatexDistributionType.DOCKER_MIKTEX
        if (isMiktex) {
            "docker volume create --name miktex".runCommand()
        }

        val sdk = LatexSdkUtil.getAllLatexSdks().firstOrNull { it.sdkType is DockerSdk }
        val dockerExecutable = if (sdk == null) {
            "docker"
        }
        else {
            (sdk.sdkType as DockerSdk).getExecutableName("docker", sdk.homePath!!)
        }

        val parameterList = mutableListOf(
            dockerExecutable,
            "run",
            "--rm",
        )

        parameterList += if (isMiktex) {
            listOf(
                "-v",
                "miktex:/miktex/.miktex",
                "-v",
                "${session.mainFile.parent.path}:/miktex/work",
            )
        }
        else {
            listOf(
                "-v",
                "${session.mainFile.parent.path}:/workdir",
            )
        }

        if (dockerOutputDir != null && session.outputDir != session.mainFile.parent) {
            parameterList += listOf("-v", "${session.outputDir.path}:$dockerOutputDir")
        }

        val auxDir = session.auxDir
        if (dockerAuxDir != null && auxDir != null && auxDir != session.mainFile.parent) {
            parameterList += listOf("-v", "${auxDir.path}:$dockerAuxDir")
        }

        val sdkImage = (sdk?.sdkAdditionalData as? DockerSdkAdditionalData)?.imageName
        val defaultImage = if (isMiktex) "miktex/miktex:latest" else "texlive/texlive:latest"
        parameterList += sdkImage ?: defaultImage
        command.addAll(0, parameterList)
    }
}
