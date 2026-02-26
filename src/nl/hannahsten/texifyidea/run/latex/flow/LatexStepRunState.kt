package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.hannahsten.texifyidea.run.latex.LatexSessionInitializer
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
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
        val session = LatexSessionInitializer.initialize(runConfig, environment)
        val mainFile = session.resolvedMainFile
            ?: throw ExecutionException("Main file cannot be resolved")

        val configuredPlan = LatexRunStepPlanBuilder.build(configuredSteps)
        if (configuredPlan.unsupportedTypes.isNotEmpty()) {
            Log.warn("Unsupported compile-step types: ${configuredPlan.unsupportedTypes.joinToString(", ")}")
        }

        if (configuredPlan.steps.isEmpty()) {
            throw ExecutionException("No executable steps found in compile-step schema.")
        }

        val context = LatexRunStepContext(runConfig, environment, session, mainFile)
        val overallHandler = StepAwareSequentialProcessHandler(configuredPlan.steps, context)
        val stepLogConsole = LatexStepLogTabComponent(environment.project, mainFile, overallHandler)

        return DefaultExecutionResult(stepLogConsole, overallHandler)
    }
}
