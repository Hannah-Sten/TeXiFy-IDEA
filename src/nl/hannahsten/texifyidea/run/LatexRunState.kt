package nl.hannahsten.texifyidea.run

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
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole
import nl.hannahsten.texifyidea.util.magic.CompilerMagic

/**
 * State of the [LatexRunConfiguration], previously called LatexCommandLineState.
 *
 * @author Sten Wessel
 */
class LatexRunState(private val runConfig: LatexRunConfiguration, private val env: ExecutionEnvironment) : RunProfileState {

    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult {
        FileDocumentManager.getInstance().saveAllDocuments()

        val console = LatexExecutionConsole(runConfig)

        if (!runConfig.options.hasBeenRun) {
            firstRunSetup(runConfig)
        }

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

    /**
     * Some extra checks only relevant when the run config has never been run.
     */
    private fun firstRunSetup(runConfig: LatexRunConfiguration) {
        // Allow each step provider to check whether such a step (or steps) needs to be added to the run config
        // The reason we check it here instead of when creating the run config, is that these checks are potentially very expensive, so it would lead to blocked or confusing UI.
        // However, when the run configuration has started running, the user is expecting it to take time, and there is a progress indicator (better would be to create a progress indicator for this setup though)
        for (provider in CompilerMagic.compileStepProviders.values) {
            provider.createIfRequired(runConfig)
        }
    }

    private fun createHandler(command: List<String>, workingDirectory: String): KillableProcessHandler {
        val commandLine = GeneralCommandLine(command)
            .withWorkDirectory(workingDirectory)
            .withEnvironment(runConfig.envs)

        return KillableProcessHandler(commandLine)
    }
}
