package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Companion.toWslPathIfNeeded
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.settings.sdk.DockerSdk
import nl.hannahsten.texifyidea.settings.sdk.DockerSdkAdditionalData
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.runCommand

object LatexmkCommandBuilder {

    private data class LatexmkDirectories(
        val outputPath: String,
        val auxilPath: String?,
        val shouldPassAuxilPath: Boolean,
        val hostOutputPath: String?,
        val hostAuxilPath: String?,
    )

    fun buildStructuredArguments(runConfig: LatexmkRunConfiguration): String {
        val arguments = mutableListOf<String>()
        val hasRcFile = LatexmkRcFileFinder.hasLatexmkRc(runConfig.compilerArguments, runConfig.getResolvedWorkingDirectory())

        val hasExplicitStructuredOptions =
            runConfig.engineMode != LatexmkEngineMode.PDFLATEX ||
                runConfig.latexmkOutputFormat != LatexmkOutputFormat.DEFAULT ||
                runConfig.citationTool != LatexmkCitationTool.AUTO ||
                !runConfig.customEngineCommand.isNullOrBlank()

        if (!hasRcFile || hasExplicitStructuredOptions) {
            arguments += runConfig.engineMode.toLatexmkFlags(runConfig.customEngineCommand)
            arguments += runConfig.latexmkOutputFormat.toLatexmkFlags()
            arguments += runConfig.citationTool.toLatexmkFlags()
        }

        runConfig.extraArguments?.let { arguments += ParametersListUtil.parse(it) }

        return ParametersListUtil.join(arguments)
    }

    fun buildCommand(runConfig: LatexmkRunConfiguration, project: Project): List<String>? {
        val mainFile = runConfig.mainFile ?: return null
        val distribution = runConfig.getLatexDistributionType()
        val directories = resolveLatexmkDirectories(runConfig, mainFile, distribution)

        val executable = runConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
            LatexCompiler.LATEXMK.executableName,
            project,
            runConfig.getLatexSdk(),
            distribution,
        )
        val command = mutableListOf(executable)

        val compilerArguments = runConfig.compilerArguments
        if (!compilerArguments.isNullOrBlank()) {
            command += ParametersListUtil.parse(compilerArguments)
        }
        command.add("-interaction=nonstopmode")
        command.add("-file-line-error")

        command.add("-outdir=${directories.outputPath}")
        if (directories.shouldPassAuxilPath) {
            command.add("-auxdir=${directories.auxilPath}")
        }

        if (distribution == LatexDistributionType.WSL_TEXLIVE) {
            val wslCommand = buildString {
                append(GeneralCommandLine(command).commandLineString)
                append(' ')
                append(mainFile.path.toWslPathIfNeeded(distribution))
            }
            return mutableListOf(*SystemEnvironment.wslCommand, wslCommand)
        }

        if (distribution.isDocker()) {
            createDockerCommand(
                runConfig,
                directories.hostAuxilPath,
                directories.hostOutputPath,
                directories.auxilPath,
                directories.outputPath,
                mainFile,
                command,
            )
        }

        if (runConfig.beforeRunCommand?.isNotBlank() == true) {
            command.add("-usepretex=${runConfig.beforeRunCommand}")
        }

        if (runConfig.hasDefaultWorkingDirectory()) {
            command.add(mainFile.name)
        }
        else {
            command.add(mainFile.path)
        }

        return command
    }

    fun buildCleanCommand(runConfig: LatexmkRunConfiguration, cleanAll: Boolean): List<String>? {
        val mainFile = runConfig.mainFile ?: return null
        val distribution = runConfig.getLatexDistributionType()
        val directories = resolveLatexmkDirectories(runConfig, mainFile, distribution)

        val executable = runConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
            LatexCompiler.LATEXMK.executableName,
            runConfig.project,
            runConfig.getLatexSdk(),
            distribution,
        )
        val command = mutableListOf(executable)

        val compilerArguments = runConfig.compilerArguments
        if (!compilerArguments.isNullOrBlank()) {
            command += ParametersListUtil.parse(compilerArguments)
        }

        command.add("-outdir=${directories.outputPath}")
        if (directories.shouldPassAuxilPath) {
            command.add("-auxdir=${directories.auxilPath}")
        }
        command.add(if (cleanAll) "-C" else "-c")

        if (distribution == LatexDistributionType.WSL_TEXLIVE) {
            val wslCommand = buildString {
                append(GeneralCommandLine(command).commandLineString)
                append(' ')
                append(mainFile.path.toWslPathIfNeeded(distribution))
            }
            return mutableListOf(*SystemEnvironment.wslCommand, wslCommand)
        }

        if (distribution.isDocker()) {
            createDockerCommand(
                runConfig,
                directories.hostAuxilPath,
                directories.hostOutputPath,
                directories.auxilPath,
                directories.outputPath,
                mainFile,
                command,
            )
        }

        if (runConfig.hasDefaultWorkingDirectory()) {
            command.add(mainFile.name)
        }
        else {
            command.add(mainFile.path)
        }

        return command
    }

    private fun resolveLatexmkDirectories(
        runConfig: LatexmkRunConfiguration,
        mainFile: VirtualFile,
        distribution: LatexDistributionType,
    ): LatexmkDirectories {
        val resolved = LatexmkPathResolver.resolveOutAuxPair(runConfig)
        val resolvedOut = resolved?.outputDir?.toString() ?: mainFile.parent.path
        val resolvedAux = resolved?.auxilDir?.toString()
        val shouldPassAux = resolved?.shouldPassAuxilDir == true

        val outputPath = when (distribution) {
            LatexDistributionType.DOCKER_MIKTEX -> "/miktex/out"
            LatexDistributionType.DOCKER_TEXLIVE -> "/out"
            else -> resolvedOut.toWslPathIfNeeded(distribution)
        }

        val auxilPath = when (distribution) {
            LatexDistributionType.DOCKER_MIKTEX -> if (shouldPassAux) "/miktex/auxil" else null
            LatexDistributionType.DOCKER_TEXLIVE -> if (shouldPassAux) "/auxil" else null
            else -> if (shouldPassAux) resolvedAux?.toWslPathIfNeeded(distribution) else null
        }

        return LatexmkDirectories(
            outputPath = outputPath,
            auxilPath = auxilPath,
            shouldPassAuxilPath = auxilPath != null && auxilPath != outputPath && shouldPassAux,
            hostOutputPath = resolvedOut,
            hostAuxilPath = if (shouldPassAux) resolvedAux else null,
        )
    }

    private fun createDockerCommand(
        runConfig: LatexmkRunConfiguration,
        hostAuxilDir: String?,
        hostOutputDir: String?,
        dockerAuxilDir: String?,
        dockerOutputDir: String?,
        mainFile: VirtualFile,
        command: MutableList<String>,
    ) {
        val isMiktex = runConfig.getLatexDistributionType() == LatexDistributionType.DOCKER_MIKTEX

        if (isMiktex) {
            "docker volume create --name miktex".runCommand()
        }

        val sdk = LatexSdkUtil.getAllLatexSdks().firstOrNull { it.sdkType is DockerSdk }

        val parameterList = mutableListOf(
            if (sdk == null) "docker" else (sdk.sdkType as DockerSdk).getExecutableName("docker", sdk.homePath!!),
            "run",
            "--rm",
        )

        parameterList += if (isMiktex) {
            listOf(
                "-v", "miktex:/miktex/.miktex",
                "-v", "${mainFile.parent.path}:/miktex/work",
            )
        }
        else {
            listOf(
                "-v", "${mainFile.parent.path}:/workdir",
            )
        }

        if (dockerOutputDir != null && hostOutputDir != null && hostOutputDir != mainFile.parent.path) {
            parameterList.addAll(listOf("-v", "$hostOutputDir:$dockerOutputDir"))
        }

        if (dockerAuxilDir != null && hostAuxilDir != null && hostAuxilDir != mainFile.parent.path) {
            parameterList.addAll(listOf("-v", "$hostAuxilDir:$dockerAuxilDir"))
        }

        val sdkImage = (sdk?.sdkAdditionalData as? DockerSdkAdditionalData)?.imageName
        val default = if (isMiktex) "miktex/miktex:latest" else "texlive/texlive:latest"
        parameterList.add(sdkImage ?: default)

        command.addAll(0, parameterList)
    }
}

private fun LatexmkEngineMode.toLatexmkFlags(customEngineCommand: String?): List<String> = when (this) {
    LatexmkEngineMode.PDFLATEX -> listOf("-pdf")
    LatexmkEngineMode.XELATEX -> listOf("-xelatex")
    LatexmkEngineMode.LUALATEX -> listOf("-lualatex")
    LatexmkEngineMode.LATEX -> listOf("-latex")
    LatexmkEngineMode.CUSTOM_COMMAND -> customEngineCommand?.let {
        val escaped = it.replace("\"", "\\\"")
        listOf("-pdflatex=\"$escaped\"")
    } ?: emptyList()
}

private fun LatexmkOutputFormat.toLatexmkFlags(): List<String> = when (this) {
    LatexmkOutputFormat.DEFAULT -> emptyList()
    LatexmkOutputFormat.PDF -> listOf("-pdf")
    LatexmkOutputFormat.DVI -> listOf("-dvi")
    LatexmkOutputFormat.PS -> listOf("-ps")
    LatexmkOutputFormat.XDV -> listOf("-xdv")
}

private fun LatexmkCitationTool.toLatexmkFlags(): List<String> = when (this) {
    LatexmkCitationTool.AUTO -> emptyList()
    LatexmkCitationTool.BIBTEX -> listOf("-bibtex")
    LatexmkCitationTool.BIBER -> listOf("-use-biber")
    LatexmkCitationTool.DISABLED -> listOf("-bibtex-")
}
