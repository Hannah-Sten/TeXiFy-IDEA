package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.DockerCommandSupport
import nl.hannahsten.texifyidea.run.compiler.LatexCompilePrograms
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Companion.toWslPathIfNeeded
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.run.latex.LatexSessionInitializer
import nl.hannahsten.texifyidea.run.latex.step.LatexmkCompileRunStep
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.SystemEnvironment
import java.nio.file.Path

object LatexmkCleanUtil {

    enum class Mode(val label: String) {
        CLEAN("Clean auxiliary files"),
        CLEAN_ALL("Clean all generated files"),
    }

    fun run(project: Project, runConfig: LatexRunConfiguration, mode: Mode) {
        if (!runConfig.hasEnabledLatexmkStep()) {
            Notification("LaTeX", "Latexmk clean failed", "Selected run configuration is not using latexmk.", NotificationType.ERROR).notify(project)
            return
        }

        val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig)
        if (mainFile == null) {
            Notification("LaTeX", "Latexmk clean failed", "No main file is configured.", NotificationType.ERROR).notify(project)
            return
        }

        val command = buildCleanCommandForModel(runConfig, mainFile, mode == Mode.CLEAN_ALL)
        if (command == null) {
            Notification("LaTeX", "Latexmk clean failed", "Could not build latexmk clean command.", NotificationType.ERROR).notify(project)
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            val workingDirectoryPath = LatexPathResolver.resolve(runConfig.workingDirectory, mainFile, project) ?: Path.of(mainFile.parent.path)
            val envVariables = runConfig.environmentVariables.envs.let { envs ->
                if (!runConfig.expandMacrosEnvVariables) {
                    envs
                }
                else {
                    val configurator = ProgramParametersConfigurator()
                    envs.mapValues { configurator.expandPathAndMacros(it.value, null, project) }
                }
            }

            runCatching {
                val process = GeneralCommandLine(command)
                    .withWorkingDirectory(workingDirectoryPath)
                    .withEnvironment(envVariables)
                    .toProcessBuilder()
                    .redirectErrorStream(true)
                    .start()

                process.inputReader().readText()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    Notification("LaTeX", "Latexmk clean completed", "Finished ${mode.label.lowercase()} for ${mainFile.name}.", NotificationType.INFORMATION).notify(project)
                }
                else {
                    Notification("LaTeX", "Latexmk clean failed", "latexmk exited with code $exitCode.", NotificationType.ERROR).notify(project)
                }
            }.onFailure {
                Notification("LaTeX", "Latexmk clean failed", it.message ?: "Unknown error.", NotificationType.ERROR).notify(project)
            }
        }
    }

    internal fun buildCleanCommandForModel(
        runConfig: LatexRunConfiguration,
        mainFile: VirtualFile,
        cleanAll: Boolean,
    ): List<String>? = buildCleanCommand(runConfig, mainFile, cleanAll)

    private fun buildCleanCommand(runConfig: LatexRunConfiguration, mainFile: VirtualFile, cleanAll: Boolean): List<String>? {
        val latexmkStep = runConfig.primaryCompileStep() as? LatexmkCompileStepOptions ?: return null
        val session = LatexSessionInitializer.initializeForModel(runConfig, mainFile)
        val distributionType = runConfig.getLatexDistributionType()
        val executable = latexmkStep.compilerPath ?: LatexSdkUtil.getExecutableName(
            LatexCompilePrograms.LATEXMK_EXECUTABLE,
            runConfig.project,
            runConfig.getLatexSdk(),
            distributionType,
        )

        val outputPath = when (distributionType) {
            LatexDistributionType.DOCKER_MIKTEX -> "/miktex/out"
            LatexDistributionType.DOCKER_TEXLIVE -> "/out"
            else -> (LatexPathResolver.resolveOutputDir(runConfig, mainFile)?.path ?: mainFile.parent.path)
                .toWslPathIfNeeded(distributionType)
        }
        val auxPath = when (distributionType) {
            LatexDistributionType.DOCKER_MIKTEX -> "/miktex/auxil"
            LatexDistributionType.DOCKER_TEXLIVE -> null
            else -> LatexPathResolver.resolveAuxDir(runConfig, mainFile)?.path?.toWslPathIfNeeded(distributionType)
        }

        val command = mutableListOf(
            executable,
            "-outdir=$outputPath",
        )
        if (auxPath != null && auxPath != outputPath) {
            command += "-auxdir=$auxPath"
        }

        val compilerArguments = LatexmkCompileRunStep.buildArguments(runConfig, session, latexmkStep)
        if (compilerArguments.isNotBlank()) {
            command += ParametersListUtil.parse(compilerArguments)
        }

        if (distributionType.isDocker()) {
            DockerCommandSupport.prependDockerRunCommand(
                session = session,
                command = command,
                dockerOutputDir = outputPath,
                dockerAuxDir = auxPath,
            )
        }

        command += if (cleanAll) "-C" else "-c"

        if (distributionType == LatexDistributionType.WSL_TEXLIVE) {
            val mainFileArg = if (runConfig.hasDefaultWorkingDirectory()) {
                mainFile.name
            }
            else {
                mainFile.path.toWslPathIfNeeded(distributionType)
            }
            var wslCommand = GeneralCommandLine(command).commandLineString
            wslCommand += " $mainFileArg"
            return mutableListOf(*SystemEnvironment.wslCommand, wslCommand)
        }

        command += if (runConfig.hasDefaultWorkingDirectory()) mainFile.name else mainFile.path
        return command
    }
}
