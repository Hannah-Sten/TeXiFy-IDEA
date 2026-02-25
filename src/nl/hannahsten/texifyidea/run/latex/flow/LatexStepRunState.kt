package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.hannahsten.texifyidea.run.latex.LatexExecutionStateInitializer
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepAutoInference
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepPlanBuilder
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.run.latex.steplog.LatexStepLogTabComponent
import nl.hannahsten.texifyidea.util.Log

internal class LatexStepRunState(
    private val runConfig: LatexRunConfiguration,
    private val environment: ExecutionEnvironment,
    private val configuredSteps: List<LatexStepRunConfigurationOptions>,
) : com.intellij.execution.configurations.RunProfileState {

    @Throws(ExecutionException::class)
    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult {
        FileDocumentManager.getInstance().saveAllDocuments()
        LatexExecutionStateInitializer.initialize(runConfig, environment, runConfig.executionState)
        val mainFile = runConfig.executionState.resolvedMainFile
            ?: throw ExecutionException("Main file cannot be resolved")

        val effectiveSteps = LatexRunStepAutoInference.augmentSteps(runConfig, mainFile, configuredSteps)
        val baseTypes = configuredSteps.map { it.type }
        val effectiveTypes = effectiveSteps.map { it.type }
        if (effectiveTypes != baseTypes || effectiveSteps.size != configuredSteps.size) {
            Log.debug("Auto-inferred compile-step sequence: ${effectiveSteps.joinToString(" -> ") { it.type }}")
        }
        val effectivePlan = LatexRunStepPlanBuilder.build(effectiveSteps)
        if (effectivePlan.unsupportedTypes.isNotEmpty()) {
            Log.warn("Unsupported compile-step types after auto-inference: ${effectivePlan.unsupportedTypes.joinToString(", ")}")
        }

        if (effectivePlan.steps.isEmpty()) {
            throw ExecutionException("No executable steps found in compile-step schema.")
        }

        val context = LatexRunStepContext(runConfig, environment, runConfig.executionState, mainFile)
        val overallHandler = StepAwareSequentialProcessHandler(effectivePlan.steps, context)
        val stepLogConsole = LatexStepLogTabComponent(environment.project, mainFile, overallHandler)

        return DefaultExecutionResult(stepLogConsole, overallHandler)
    }
}
