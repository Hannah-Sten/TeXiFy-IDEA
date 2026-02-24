package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.common.createCompilationHandler

internal class LatexCompileRunStep(
    private val stepConfig: LatexStepRunConfigurationOptions,
) : LatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val runConfig = context.runConfig
        runConfig.activateStepForExecution(configId)
        try {
            val compiler = resolveCompiler(stepConfig)
            val command = compiler.getCommand(runConfig, context.environment.project)
                ?: throw ExecutionException("Compile command could not be created.")
            val programParamsConfigurator = ProgramParametersConfigurator()

            return createCompilationHandler(
                environment = context.environment,
                mainFile = context.mainFile,
                command = command,
                workingDirectory = context.executionState.resolvedWorkingDirectory,
                expandMacrosEnvVariables = runConfig.expandMacrosEnvVariables,
                envs = runConfig.environmentVariables.envs,
                expandEnvValue = { value -> expandPathAndMacros(programParamsConfigurator, value, runConfig) },
            ).also { handler ->
                handler.addProcessListener(object : ProcessAdapter() {
                    override fun processTerminated(event: ProcessEvent) {
                        context.executionState.markHasRun()
                    }
                })
            }
        }
        finally {
            runConfig.activateStepForExecution(null)
        }
    }

    private fun expandPathAndMacros(
        configurator: ProgramParametersConfigurator,
        value: String,
        runConfig: LatexRunConfiguration
    ): String = configurator.expandPathAndMacros(value, null, runConfig.project) ?: value

    private fun resolveCompiler(stepConfig: LatexStepRunConfigurationOptions): LatexCompiler = when (stepConfig) {
        is LatexCompileStepOptions -> stepConfig.compiler
        is LatexmkCompileStepOptions -> LatexCompiler.LATEXMK
        else -> throw ExecutionException("Step ${stepConfig.type} cannot be executed by LatexCompileRunStep.")
    }
}
