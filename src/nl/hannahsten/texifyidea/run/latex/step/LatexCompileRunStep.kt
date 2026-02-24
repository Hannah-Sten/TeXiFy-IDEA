package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.common.createCompilationHandler

internal class LatexCompileRunStep(
    override val id: String = "latex-compile",
    private val compilerOverride: LatexCompiler? = null,
) : LatexRunStep {

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val runConfig = context.runConfig
        val compiler = compilerOverride ?: runConfig.compiler ?: throw ExecutionException("No valid compiler specified.")
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

    private fun expandPathAndMacros(
        configurator: ProgramParametersConfigurator,
        value: String,
        runConfig: LatexRunConfiguration
    ): String = configurator.expandPathAndMacros(value, null, runConfig.project) ?: value
}
