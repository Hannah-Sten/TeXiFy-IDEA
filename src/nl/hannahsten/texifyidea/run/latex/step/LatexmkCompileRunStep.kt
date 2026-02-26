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
import nl.hannahsten.texifyidea.run.common.DockerCommandSupport
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.compiler.LatexCompilePrograms
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Companion.toWslPathIfNeeded
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.latexmk.buildLatexmkStructuredArguments
import nl.hannahsten.texifyidea.run.latexmk.compileModeFromMagicCommand
import nl.hannahsten.texifyidea.run.latexmk.preferredCompileModeForPackages
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
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
        val command = buildCommand(session, stepConfig, effectiveArguments)
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
            LatexDistributionType.DOCKER_MIKTEX -> "/miktex/out"
            LatexDistributionType.DOCKER_TEXLIVE -> "/out"
            else -> session.outputDir.path.toWslPathIfNeeded(session.distributionType)
        }
        val auxPath = when (session.distributionType) {
            LatexDistributionType.DOCKER_MIKTEX -> "/miktex/auxil"
            LatexDistributionType.DOCKER_TEXLIVE -> null
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

        if (session.distributionType == LatexDistributionType.WSL_TEXLIVE) {
            var wslCommand = GeneralCommandLine(command).commandLineString
            if (effectiveArguments.isNotBlank()) {
                ParametersListUtil.parse(effectiveArguments).forEach { wslCommand += " $it" }
            }
            wslCommand += " ${session.mainFile.path.toWslPathIfNeeded(session.distributionType)}"
            return mutableListOf(*SystemEnvironment.wslCommand, wslCommand)
        }

        if (session.distributionType.isDocker()) {
            DockerCommandSupport.prependDockerRunCommand(
                session = session,
                command = command,
                dockerOutputDir = outputPath,
                dockerAuxDir = auxPath,
            )
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
