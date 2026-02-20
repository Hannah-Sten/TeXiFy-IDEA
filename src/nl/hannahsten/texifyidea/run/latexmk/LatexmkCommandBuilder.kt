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
            runConfig.compileMode != LatexmkCompileMode.PDFLATEX_PDF ||
                runConfig.citationTool != LatexmkCitationTool.AUTO ||
                !runConfig.customEngineCommand.isNullOrBlank()

        if (!hasRcFile || hasExplicitStructuredOptions) {
            arguments += runConfig.compileMode.toLatexmkFlags(runConfig.customEngineCommand)
            arguments += runConfig.citationTool.toLatexmkFlags()
        }

        runConfig.extraArguments?.let { arguments += ParametersListUtil.parse(it) }

        return ParametersListUtil.join(arguments)
    }

    fun buildCommand(runConfig: LatexmkRunConfiguration, project: Project): List<String>? = buildLatexmkCommand(
        runConfig = runConfig,
        project = project,
        additionalArguments = { command, _ ->
            command.add("-interaction=nonstopmode")
            command.add("-file-line-error")
            if (runConfig.beforeRunCommand?.isNotBlank() == true) {
                command.add("-usepretex=${runConfig.beforeRunCommand}")
            }
        }
    )

    fun buildCleanCommand(runConfig: LatexmkRunConfiguration, cleanAll: Boolean): List<String>? = buildLatexmkCommand(
        runConfig = runConfig,
        project = null,
        additionalArguments = { _, _ -> },
        cleanMode = if (cleanAll) "-C" else "-c"
    )

    private fun buildLatexmkCommand(
        runConfig: LatexmkRunConfiguration,
        project: Project?,
        additionalArguments: (MutableList<String>, VirtualFile) -> Unit,
        cleanMode: String? = null,
    ): List<String>? {
        val mainFile = runConfig.resolveMainFileIfNeeded() ?: return null
        val distribution = runConfig.getLatexDistributionType()
        val directories = resolveLatexmkDirectories(runConfig, mainFile, distribution)

        val executable = runConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
            LatexCompiler.LATEXMK.executableName,
            project ?: runConfig.project,
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

        if (cleanMode != null) {
            command.add(cleanMode)
        }

        additionalArguments(command, mainFile)

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
                distribution,
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
            LatexDistributionType.DOCKER_MIKTEX -> if (resolvedOut == mainFile.parent.path) "/miktex/work" else "/miktex/out"
            LatexDistributionType.DOCKER_TEXLIVE -> if (resolvedOut == mainFile.parent.path) "/workdir" else "/out"
            else -> resolvedOut.toWslPathIfNeeded(distribution)
        }

        val auxilPath = when (distribution) {
            LatexDistributionType.DOCKER_MIKTEX -> {
                if (!shouldPassAux) null
                else if (resolvedAux == mainFile.parent.path) "/miktex/work"
                else "/miktex/auxil"
            }
            LatexDistributionType.DOCKER_TEXLIVE -> {
                if (!shouldPassAux) null
                else if (resolvedAux == mainFile.parent.path) "/workdir"
                else "/auxil"
            }
            else -> if (shouldPassAux) resolvedAux?.toWslPathIfNeeded(distribution) else null
        }

        return LatexmkDirectories(
            outputPath = outputPath,
            auxilPath = auxilPath,
            shouldPassAuxilPath = auxilPath != null && auxilPath != outputPath,
            hostOutputPath = resolvedOut,
            hostAuxilPath = if (shouldPassAux) resolvedAux else null,
        )
    }

    private fun createDockerCommand(
        distribution: LatexDistributionType,
        hostAuxilDir: String?,
        hostOutputDir: String?,
        dockerAuxilDir: String?,
        dockerOutputDir: String?,
        mainFile: VirtualFile,
        command: MutableList<String>,
    ) {
        val isMiktex = distribution == LatexDistributionType.DOCKER_MIKTEX

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

private fun LatexmkCompileMode.toLatexmkFlags(customEngineCommand: String?): List<String> = when (this) {
    LatexmkCompileMode.PDFLATEX_PDF -> listOf("-pdf")
    LatexmkCompileMode.LUALATEX_PDF -> listOf("-lualatex")
    LatexmkCompileMode.XELATEX_PDF -> listOf("-xelatex")
    LatexmkCompileMode.LATEX_DVI -> listOf("-latex", "-dvi")
    LatexmkCompileMode.XELATEX_XDV -> listOf("-xelatex", "-xdv")
    LatexmkCompileMode.LATEX_PS -> listOf("-latex", "-ps")
    LatexmkCompileMode.CUSTOM -> customEngineCommand?.let {
        val escaped = it.replace("\"", "\\\"")
        listOf("-pdflatex=\"$escaped\"")
    } ?: emptyList()
}

private fun LatexmkCitationTool.toLatexmkFlags(): List<String> = when (this) {
    LatexmkCitationTool.AUTO -> emptyList()
    LatexmkCitationTool.BIBTEX -> listOf("-bibtex")
    LatexmkCitationTool.BIBER -> listOf("-use-biber")
    LatexmkCitationTool.DISABLED -> listOf("-bibtex-")
}
