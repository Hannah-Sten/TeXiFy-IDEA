package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.util.io.awaitExit
import nl.hannahsten.texifyidea.util.runInBackgroundWithoutProgress
import java.nio.file.Path

object LatexmkCleanUtil {

    enum class Mode(val label: String) {
        CLEAN("Clean auxiliary files"),
        CLEAN_ALL("Clean all generated files"),
    }

    fun run(project: Project, runConfig: LatexmkRunConfiguration, mode: Mode) {
        val mainFile = runConfig.mainFile
        if (mainFile == null) {
            Notification("LaTeX", "Latexmk clean failed", "No main file is configured.", NotificationType.ERROR).notify(project)
            return
        }

        val command = LatexmkCommandBuilder.buildCleanCommand(runConfig, mode == Mode.CLEAN_ALL)
        if (command == null) {
            Notification("LaTeX", "Latexmk clean failed", "Could not build latexmk clean command.", NotificationType.ERROR).notify(project)
            return
        }

        runInBackgroundWithoutProgress {
            val workingDirectoryPath = runConfig.getResolvedWorkingDirectory() ?: Path.of(mainFile.parent.path)
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
}
