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
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepAutoInference
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepPlanBuilder
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepPlan
import nl.hannahsten.texifyidea.util.Log

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

        val baseTypes = plan.steps.map { it.id }
        val effectiveTypes = LatexRunStepAutoInference.augmentStepTypes(runConfig, mainFile, baseTypes)
        if (effectiveTypes != baseTypes) {
            Log.debug("Auto-inferred compile-step sequence: ${effectiveTypes.joinToString(" -> ")}")
        }
        val effectivePlan = LatexRunStepPlanBuilder.build(effectiveTypes)
        if (effectivePlan.unsupportedTypes.isNotEmpty()) {
            Log.warn("Unsupported compile-step types after auto-inference: ${effectivePlan.unsupportedTypes.joinToString(", ")}")
        }

        val handlers = effectivePlan.steps.map { step -> step.createProcess(context) }
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
