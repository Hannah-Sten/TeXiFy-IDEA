package nl.hannahsten.texifyidea.run

import com.intellij.build.events.MessageEvent
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Key
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.run.ui.console.logtab.ui.LatexCompileMessageTreeView
import nl.hannahsten.texifyidea.run.ui.console.LatexExecutionConsole

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
            val command = step.getCommand() ?: return@mapNotNull null
            val workingDirectory = step.getWorkingDirectory() ?: ProjectUtil.getBaseDir()

            val commandLine = GeneralCommandLine(command)
                .withWorkDirectory(workingDirectory)
                .withEnvironment(runConfig.envs)

            val handler = KillableProcessHandler(commandLine)
            handler.addProcessListener(object : ProcessAdapter() {
                override fun startNotified(event: ProcessEvent) {
                    console.startStep(i.toString(), step, handler)
                }

                override fun processTerminated(event: ProcessEvent) {
                    console.finishStep(i.toString(), event.exitCode)
                }

                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
//                    if (outputType !is ProcessOutputType) return
//                    buildView.onEvent(id, OutputBuildEventImpl(id, "latex", event.text, outputType.isStdout))
                }
            })

            val latexOutputListener = LatexOutputListener(
                runConfig.project,
                runConfig.mainFile,
                mutableListOf(),
                mutableListOf(),
                LatexCompileMessageTreeView(runConfig.project, mutableListOf(), mutableListOf())
            )
            latexOutputListener.newMessageListener = { message, file ->
                val type = when (message.type) {
                    LatexLogMessageType.WARNING -> MessageEvent.Kind.WARNING
                    LatexLogMessageType.ERROR -> MessageEvent.Kind.ERROR
                }

//                val event = if (file != null) {
//                    FileMessageEventImpl(id, type, id, message.message, null, FilePosition(File(file.path), message.line, 0))
//                }
//                else {
//                    MessageEventImpl(id, type, id, message.message, null)
//                }
//                buildView.onEvent(id, event)
            }
            handler.addProcessListener(latexOutputListener)

            handler
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
