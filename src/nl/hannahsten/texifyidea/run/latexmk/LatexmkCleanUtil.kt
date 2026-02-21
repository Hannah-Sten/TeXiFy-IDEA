package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.awaitExit
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.run.latex.LatexmkModeService
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.runInBackgroundWithoutProgress
import java.nio.file.Path

object LatexmkCleanUtil {

    enum class Mode(val label: String) {
        CLEAN("Clean auxiliary files"),
        CLEAN_ALL("Clean all generated files"),
    }

    fun run(project: Project, runConfig: LatexRunConfiguration, mode: Mode) {
        if (runConfig.compiler != LatexCompiler.LATEXMK) {
            Notification("LaTeX", "Latexmk clean failed", "Selected run configuration is not using latexmk.", NotificationType.ERROR).notify(project)
            return
        }

        val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig)
        if (mainFile == null) {
            Notification("LaTeX", "Latexmk clean failed", "No main file is configured.", NotificationType.ERROR).notify(project)
            return
        }

        val command = buildCleanCommand(runConfig, mainFile, mode == Mode.CLEAN_ALL)
        if (command == null) {
            Notification("LaTeX", "Latexmk clean failed", "Could not build latexmk clean command.", NotificationType.ERROR).notify(project)
            return
        }

        runInBackgroundWithoutProgress {
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
                val exitCode = process.awaitExit()

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

    private fun buildCleanCommand(runConfig: LatexRunConfiguration, mainFile: VirtualFile, cleanAll: Boolean): List<String> {
        val distributionType = runConfig.getLatexDistributionType()
        val executable = runConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
            LatexCompiler.LATEXMK.executableName,
            runConfig.project,
            runConfig.getLatexSdk(),
            distributionType,
        )

        val command = mutableListOf(executable)
        val compilerArguments = LatexmkModeService.buildArguments(runConfig)
        if (compilerArguments.isNotBlank()) {
            command += ParametersListUtil.parse(compilerArguments)
        }

        val outputPath = LatexPathResolver.resolveOutputDir(runConfig, mainFile)?.path ?: mainFile.parent.path
        command += "-outdir=$outputPath"

        val auxPath = LatexPathResolver.resolveAuxDir(runConfig, mainFile)?.path
        if (auxPath != null && auxPath != outputPath) {
            command += "-auxdir=$auxPath"
        }

        command += if (cleanAll) "-C" else "-c"
        command += if (runConfig.hasDefaultWorkingDirectory()) mainFile.name else mainFile.path
        return command
    }
}
