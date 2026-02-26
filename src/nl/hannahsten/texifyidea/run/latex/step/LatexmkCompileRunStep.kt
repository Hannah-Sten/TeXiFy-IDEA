package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiManager
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.compiler.LatexCompilePrograms
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Companion.toWslPathIfNeeded
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.latexmk.buildLatexmkStructuredArguments
import nl.hannahsten.texifyidea.run.latexmk.compileModeFromMagicCommand
import nl.hannahsten.texifyidea.run.latexmk.preferredCompileModeForPackages
import nl.hannahsten.texifyidea.settings.sdk.DockerSdk
import nl.hannahsten.texifyidea.settings.sdk.DockerSdkAdditionalData
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.runCommand
import java.util.Locale

internal class LatexmkCompileRunStep(
    private val stepConfig: LatexmkCompileStepOptions,
) : ProcessLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val session = context.session
        val effectiveMode = effectiveCompileMode(context.runConfig, session, stepConfig)
        val effectiveArguments = buildArguments(context.runConfig, session, stepConfig, effectiveMode)
        session.resolvedOutputFilePath = outputFilePath(session, effectiveMode)
        val command = buildCommand(context.runConfig, session, stepConfig, effectiveArguments)
        val programParamsConfigurator = ProgramParametersConfigurator()

        return createCompilationHandler(
            environment = context.environment,
            mainFile = session.mainFile,
            command = command,
            workingDirectory = session.workingDirectory,
            expandMacrosEnvVariables = context.runConfig.expandMacrosEnvVariables,
            envs = context.runConfig.environmentVariables.envs,
            expandEnvValue = { value ->
                programParamsConfigurator.expandPathAndMacros(value, null, context.runConfig.project) ?: value
            },
        )
    }

    internal fun buildCommand(
        runConfig: LatexRunConfiguration,
        session: LatexRunSessionState,
        step: LatexmkCompileStepOptions,
        effectiveArguments: String,
    ): List<String> {
        val executable = step.compilerPath ?: LatexSdkUtil.getExecutableName(
            LatexCompilePrograms.LATEXMK_EXECUTABLE,
            session.project,
            session.latexSdk,
            session.distributionType,
        )
        val outputPath = when (session.distributionType) {
            nl.hannahsten.texifyidea.run.latex.LatexDistributionType.DOCKER_MIKTEX -> "/miktex/out"
            nl.hannahsten.texifyidea.run.latex.LatexDistributionType.DOCKER_TEXLIVE -> "/out"
            else -> session.outputDir.path.toWslPathIfNeeded(session.distributionType)
        }
        val auxPath = when (session.distributionType) {
            nl.hannahsten.texifyidea.run.latex.LatexDistributionType.DOCKER_MIKTEX -> "/miktex/auxil"
            nl.hannahsten.texifyidea.run.latex.LatexDistributionType.DOCKER_TEXLIVE -> null
            else -> session.auxDir?.path?.toWslPathIfNeeded(session.distributionType)
        }

        val command = mutableListOf(
            executable,
            "-file-line-error",
            "-interaction=nonstopmode",
            "-outdir=$outputPath",
        )
        if (auxPath != null && auxPath != outputPath) {
            command += "-auxdir=$auxPath"
        }

        if (session.distributionType == nl.hannahsten.texifyidea.run.latex.LatexDistributionType.WSL_TEXLIVE) {
            var wslCommand = GeneralCommandLine(command).commandLineString
            if (effectiveArguments.isNotBlank()) {
                ParametersListUtil.parse(effectiveArguments).forEach { wslCommand += " $it" }
            }
            wslCommand += " ${session.mainFile.path.toWslPathIfNeeded(session.distributionType)}"
            return mutableListOf(*SystemEnvironment.wslCommand, wslCommand)
        }

        if (session.distributionType.isDocker()) {
            createDockerCommand(session, outputPath, auxPath, command)
        }

        if (effectiveArguments.isNotBlank()) {
            ParametersListUtil.parse(effectiveArguments).forEach(command::add)
        }

        if (step.beforeRunCommand?.isNotBlank() == true) {
            command += "-usepretex=${step.beforeRunCommand}"
            command += session.mainFile.name
        }
        else if (session.usesDefaultWorkingDirectory) {
            command += session.mainFile.name
        }
        else {
            command += session.mainFile.path
        }

        return command
    }

    private fun createDockerCommand(
        session: LatexRunSessionState,
        dockerOutputDir: String,
        dockerAuxDir: String?,
        command: MutableList<String>,
    ) {
        val isMiktex = session.distributionType == nl.hannahsten.texifyidea.run.latex.LatexDistributionType.DOCKER_MIKTEX
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

        if (session.outputDir != session.mainFile.parent) {
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

    companion object {

        fun buildArguments(
            runConfig: LatexRunConfiguration,
            session: LatexRunSessionState,
            step: LatexmkCompileStepOptions,
            effectiveCompileModeOverride: LatexmkCompileMode? = null,
        ): String {
            val hasRcFile = LatexmkRcFileFinder.hasLatexmkRc(step.compilerArguments, session.workingDirectory)
            val effectiveCompileMode = effectiveCompileModeOverride ?: effectiveCompileMode(runConfig, session, step)
            return buildLatexmkStructuredArguments(
                hasRcFile = hasRcFile,
                compileMode = effectiveCompileMode,
                citationTool = step.latexmkCitationTool,
                customEngineCommand = step.latexmkCustomEngineCommand,
                extraArguments = step.latexmkExtraArguments,
            )
        }

        fun effectiveCompileMode(
            runConfig: LatexRunConfiguration,
            session: LatexRunSessionState,
            step: LatexmkCompileStepOptions,
        ): LatexmkCompileMode {
            if (step.latexmkCompileMode != LatexmkCompileMode.AUTO) {
                return step.latexmkCompileMode
            }

            return ReadAction.compute<LatexmkCompileMode, RuntimeException> {
                val psi = PsiManager.getInstance(runConfig.project).findFile(session.mainFile) ?: session.psiFile?.element
                val magicComments = psi?.allParentMagicComments()
                val magicMode = compileModeFromMagicCommand(
                    magicComments?.value(DefaultMagicKeys.COMPILER) ?: magicComments?.value(DefaultMagicKeys.PROGRAM),
                )
                val packageMode = psi?.let { preferredCompileModeForPackages(it.includedPackagesInFileset()) }
                magicMode ?: packageMode ?: LatexmkCompileMode.PDFLATEX_PDF
            }
        }

        fun outputFilePath(
            session: LatexRunSessionState,
            effectiveCompileMode: LatexmkCompileMode,
        ): String {
            val extension = effectiveCompileMode.extension.lowercase(Locale.getDefault())
            return "${session.outputDir.path}/${session.mainFile.nameWithoutExtension}.$extension"
        }
    }
}
