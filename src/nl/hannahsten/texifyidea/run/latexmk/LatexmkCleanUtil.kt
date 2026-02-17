package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import nl.hannahsten.texifyidea.util.runInBackgroundWithoutProgress
import kotlin.io.path.Path

object LatexmkCleanUtil {

    enum class Mode(val flag: String, val label: String) {
        CLEAN("-c", "Clean auxiliary files"),
        CLEAN_ALL("-C", "Clean all generated files"),
    }

    fun promptMode(project: Project): Mode? {
        val result = Messages.showYesNoCancelDialog(
            project,
            "Choose latexmk clean mode.\nYes = -c (auxiliary files), No = -C (all generated files).",
            "Clean With latexmk",
            "Use -c",
            "Use -C",
            "Cancel",
            null,
        )

        return when (result) {
            Messages.YES -> Mode.CLEAN
            Messages.NO -> Mode.CLEAN_ALL
            else -> null
        }
    }

    fun run(project: Project, runConfig: LatexmkRunConfiguration, mode: Mode) {
        val mainFile = runConfig.mainFile
        if (mainFile == null) {
            Notification("LaTeX", "latexmk clean failed", "No main file is configured.", NotificationType.ERROR).notify(project)
            return
        }

        val command = LatexmkCommandBuilder.buildCleanCommand(runConfig, mode == Mode.CLEAN_ALL)
        if (command == null) {
            Notification("LaTeX", "latexmk clean failed", "Could not build latexmk clean command.", NotificationType.ERROR).notify(project)
            return
        }

        runInBackgroundWithoutProgress {
            val workingDirectoryPath = runConfig.getResolvedWorkingDirectory() ?: mainFile.parent.path

            runCatching {
                val process = GeneralCommandLine(command)
                    .withWorkingDirectory(Path(workingDirectoryPath))
                    .withEnvironment(runConfig.environmentVariables.envs)
                    .toProcessBuilder()
                    .redirectErrorStream(true)
                    .start()

                process.inputReader().readText()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    Notification("LaTeX", "latexmk clean completed", "Finished ${mode.label.lowercase()} for ${mainFile.name}.", NotificationType.INFORMATION).notify(project)
                }
                else {
                    Notification("LaTeX", "latexmk clean failed", "latexmk exited with code $exitCode.", NotificationType.ERROR).notify(project)
                }
            }.onFailure {
                Notification("LaTeX", "latexmk clean failed", it.message ?: "Unknown error.", NotificationType.ERROR).notify(project)
            }
        }
    }
}
