package nl.hannahsten.texifyidea.run

import com.intellij.build.FilePosition
import com.intellij.build.events.MessageEvent
import com.intellij.build.events.impl.FileMessageEventImpl
import com.intellij.build.events.impl.MessageEventImpl
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexOutputListener
import java.io.File

/**
 * State of the [LatexRunConfiguration], previously called LatexCommandLineState.
 *
 * @author Sten Wessel
 */
class LatexRunState(private val runConfig: LatexRunConfiguration, private val env: ExecutionEnvironment) : RunProfileState {


    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult {
        FileDocumentManager.getInstance().saveAllDocuments()

        val console = LatexExecutionConsole(runConfig)

        val handlers = runConfig.compileSteps.withIndex().mapNotNull { (i, step) ->
            val id = i.toString()
            step.execute(id, console)
        }

        val overallProcessHandler = SequentialProcessHandler(handlers)

        overallProcessHandler.addProcessListener(object : ProcessAdapter() {
            override fun startNotified(event: ProcessEvent) {
                console.start()
            }

            override fun processTerminated(event: ProcessEvent) {
                console.finish(failed = event.exitCode != 0)
            }
        })

        return DefaultExecutionResult(console, overallProcessHandler)
    }

    private fun createHandler(command: List<String>, workingDirectory: String): KillableProcessHandler {
        val commandLine = GeneralCommandLine(command)
            .withWorkDirectory(workingDirectory)
            .withEnvironment(runConfig.envs)

        return KillableProcessHandler(commandLine)
    }

}
