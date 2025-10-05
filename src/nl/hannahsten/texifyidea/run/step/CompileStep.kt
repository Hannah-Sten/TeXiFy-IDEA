package nl.hannahsten.texifyidea.run.step

import com.intellij.build.FilePosition
import com.intellij.build.events.MessageEvent
import com.intellij.build.events.impl.FileMessageEventImpl
import com.intellij.build.events.impl.MessageEventImpl
import com.intellij.execution.ExecutionException
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.ide.impl.ProjectUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.util.Log
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists

/**
 * A step in the compilation process of compiling a LaTeX document.
 * Note that this doesn't have to be a LaTeX compiler, it can be any executable tool.
 *
 * @author Sten Wessel
 */
abstract class CompileStep : Step {

    override fun execute(id: String, console: LatexExecutionConsole): KillableProcessHandler {
        val command = getCommand()
        val workingDirectoryPath = getWorkingDirectory() ?: ProjectUtil.getBaseDir()

        val workingDirectory = Path(workingDirectoryPath)


        if (workingDirectory.exists().not()) {
            Notification("LaTeX", "Could not find working directory", "The directory containing the main file could not be found: $workingDirectoryPath", NotificationType.ERROR).notify(configuration.project)
            throw ExecutionException("Could not find working directory $workingDirectoryPath")
        }

        // Windows has a maximum length of a command, possibly 32k characters (#3956), so we log this info in the exception
        if (SystemInfo.isWindows && command.sumOf { it.length } > 10_000) {
            throw ExecutionException("The following command was too long to run: ${command.joinToString(" ")}")
        }
        val commandLine = GeneralCommandLine(command)
            .withWorkingDirectory(workingDirectory)
        val envVariablesWithMacro = getEnvironmentVariables()

        // todo expand macros
        @Suppress("UnstableApiUsage")
        val envVariables = envVariablesWithMacro//.applyIf(runConfig.expandMacrosEnvVariables) {
//            ExecutionManagerImpl.withEnvironmentDataContext(SimpleDataContext.getSimpleContext(CommonDataKeys.VIRTUAL_FILE, mainFile, environment.dataContext)).use {
//                mapValues { programParamsConfigurator.expandPathAndMacros(it.value, null, runConfig.project) }
//            }
//        }
        envVariables.configureCommandLine(commandLine, true)

        Log.debug("Executing ${commandLine.commandLineString} in $workingDirectory")
        val handler = KillableProcessHandler(commandLine)
        handler.addProcessListener(object : ProcessAdapter() {
            override fun startNotified(event: ProcessEvent) {
                // Make sure generated files from previous steps are recognised
                // todo refreshing is apparently not the issue, as even if the file is refreshed the old version is picked up
                LocalFileSystem.getInstance().refresh(false)

                console.startStep(id, this@CompileStep, handler)
            }

            override fun processTerminated(event: ProcessEvent) {
                console.finishStep(id, event.exitCode)
            }

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                // todo onTextAvailable - nothing to be done here?
//                    if (outputType !is ProcessOutputType) return
//                    buildView.onEvent(id, OutputBuildEventImpl(id, "latex", event.text, outputType.isStdout))
            }
        })

        val latexOutputListener = LatexOutputListener(
            configuration.project,
            configuration.options.mainFile.resolve(),
            mutableListOf(),
            mutableListOf(),
        )
        latexOutputListener.newMessageListener = { message, file ->
            val type = when (message.type) {
                LatexLogMessageType.WARNING -> MessageEvent.Kind.WARNING
                LatexLogMessageType.ERROR -> MessageEvent.Kind.ERROR
            }

            val group = "LaTeX " + type.name

            val event = if (file != null) {
                FileMessageEventImpl(id, type, group, message.message, null, FilePosition(File(file.path), message.line, 0))
            }
            else {
                MessageEventImpl(id, type, group, message.message, null)
            }

            console.onEvent(event)
//                buildView.onEvent(id, event)
        }
        handler.addProcessListener(latexOutputListener)

        return handler
    }

    abstract fun getCommand(): List<String>

    abstract fun getWorkingDirectory(): String?

    abstract fun getEnvironmentVariables(): EnvironmentVariablesData
}
