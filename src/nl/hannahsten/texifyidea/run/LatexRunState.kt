package nl.hannahsten.texifyidea.run

import com.intellij.build.BuildView
import com.intellij.build.DefaultBuildDescriptor
import com.intellij.build.FilePosition
import com.intellij.build.events.MessageEvent
import com.intellij.build.events.impl.*
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.logtab.ui.LatexCompileMessageTreeView
import nl.hannahsten.texifyidea.service.LatexRunConfigurationViewManagerService
import java.io.File

class LatexRunState(private val runConfig: LatexRunConfiguration, private val env: ExecutionEnvironment) : RunProfileState {

    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult? {
        FileDocumentManager.getInstance().saveAllDocuments()

        val descriptor = DefaultBuildDescriptor("latex", runConfig.name, ProjectUtil.getBaseDir(), System.currentTimeMillis())
        val viewManager = runConfig.project.getService(LatexRunConfigurationViewManagerService::class.java)

        val console = TextConsoleBuilderFactory.getInstance().createBuilder(runConfig.project).console
        val buildView = BuildView(runConfig.project, console, descriptor, "build.toolwindow.run.selection.state", viewManager)

        val handlers = runConfig.compileSteps.withIndex().mapNotNull { (i, step) ->
            val id = "latex-step-$i"

            val command = step.getCommand() ?: return@mapNotNull null
            val workingDirectory = step.getWorkingDirectory() ?: ProjectUtil.getBaseDir()

            val commandLine = GeneralCommandLine(command)
                .withWorkDirectory(workingDirectory)
                .withEnvironment(runConfig.envs)

            val handler = KillableProcessHandler(commandLine)
            handler.addProcessListener(object : ProcessAdapter() {
                override fun startNotified(event: ProcessEvent) {
                    buildView.onEvent(id, StartEventImpl(id, "latex", System.currentTimeMillis(), step.provider.name))
                }

                override fun processTerminated(event: ProcessEvent) {
                    if (event.exitCode == 0) {
                        buildView.onEvent(id, FinishEventImpl(id, "latex", System.currentTimeMillis(), step.provider.name, SuccessResultImpl()))
                    }
                    else {
                        buildView.onEvent(id, FinishEventImpl(id, "latex", System.currentTimeMillis(), step.provider.name, FailureResultImpl()))
                    }
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

                val event = if (file != null) {
                    FileMessageEventImpl(id, type, null, message.message, null, FilePosition(File(file.path), message.line, 0))
                }
                else {
                    MessageEventImpl(id, type, null, message.message, null)
                }
                buildView.onEvent(id, event)
            }
            handler.addProcessListener(latexOutputListener)

            handler
        }

        val processHandler = LatexStepsProcessHandler(runConfig.name, handlers)

        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun startNotified(event: ProcessEvent) {
                val d = DefaultBuildDescriptor("latex", runConfig.name, ProjectUtil.getBaseDir(), System.currentTimeMillis())
                buildView.onEvent("latex", StartBuildEventImpl(d, "Running steps..."))
            }

            override fun processTerminated(event: ProcessEvent) {
                buildView.onEvent("latex", FinishBuildEventImpl("latex", null, System.currentTimeMillis(),"Successful", SuccessResultImpl()))
            }
        })

        return DefaultExecutionResult(buildView, processHandler)
    }

    private fun createHandler(command: List<String>, workingDirectory: String): KillableProcessHandler {
        val commandLine = GeneralCommandLine(command)
            .withWorkDirectory(workingDirectory)
            .withEnvironment(runConfig.envs)

        return KillableProcessHandler(commandLine)
    }

}
