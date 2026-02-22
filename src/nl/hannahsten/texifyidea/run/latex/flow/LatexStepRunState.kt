package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.filters.RegexpFilter
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.hannahsten.texifyidea.run.latex.LatexExecutionStateInitializer
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepPlan

internal class LatexStepRunState(
    private val runConfig: LatexRunConfiguration,
    private val environment: ExecutionEnvironment,
    private val plan: LatexRunStepPlan,
) : com.intellij.execution.configurations.RunProfileState {

    @Throws(ExecutionException::class)
    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult {
        FileDocumentManager.getInstance().saveAllDocuments()
        LatexExecutionStateInitializer.initialize(runConfig, environment, runConfig.executionState)
        val mainFile = runConfig.executionState.resolvedMainFile
            ?: throw ExecutionException("Main file cannot be resolved")
        val context = LatexRunStepContext(runConfig, environment, runConfig.executionState, mainFile)
        val handlers = plan.steps.map { step -> step.createProcess(context) }
        if (handlers.isEmpty()) {
            throw ExecutionException("No executable steps found in compile-step schema.")
        }

        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(environment.project)
        val filter = RegexpFilter(environment.project, $$"^$FILE_PATH$:$LINE$")
        consoleBuilder.addFilter(filter)
        val console = consoleBuilder.console
        handlers.forEach { console.attachToProcess(it) }
        val overallHandler = SequentialProcessHandler(handlers)

        return DefaultExecutionResult(console, overallHandler)
    }
}
