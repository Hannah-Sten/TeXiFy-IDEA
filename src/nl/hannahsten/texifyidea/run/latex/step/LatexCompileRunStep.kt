package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import nl.hannahsten.texifyidea.run.latex.LatexSessionInitializer
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.common.createCompilationHandler

internal class LatexCompileRunStep(
    private val stepConfig: LatexStepRunConfigurationOptions,
) : ProcessLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    override fun afterFinish(context: LatexRunStepContext, exitCode: Int) {}

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val runConfig = context.runConfig
        runConfig.activateStepForExecution(configId)
        try {
            val session = context.session
            if (!session.isInitialized || session.resolvedMainFile == null || session.resolvedOutputDir == null) {
                LatexSessionInitializer.initialize(runConfig, context.environment, session)
            }

            LatexSessionInitializer.refreshCompileStepDerivedState(runConfig, session, stepConfig)
            val compiler = resolveCompiler(stepConfig)
            val command = compiler.getCommand(runConfig, context.environment.project, session)
                ?: throw ExecutionException(
                    buildString {
                        append("Compile command could not be created. ")
                        append("stepType=").append(stepConfig.type).append(", ")
                        append("isInitialized=").append(session.isInitialized).append(", ")
                        append("mainFile=").append(session.resolvedMainFile?.path ?: "<null>").append(", ")
                        append("outputDir=").append(session.resolvedOutputDir?.path ?: "<null>").append(", ")
                        append("workingDir=").append(session.resolvedWorkingDirectory?.toString() ?: "<null>")
                    }
                )
            val programParamsConfigurator = ProgramParametersConfigurator()

            return createCompilationHandler(
                environment = context.environment,
                mainFile = context.mainFile,
                command = command,
                workingDirectory = session.resolvedWorkingDirectory,
                expandMacrosEnvVariables = runConfig.expandMacrosEnvVariables,
                envs = runConfig.environmentVariables.envs,
                expandEnvValue = { value -> expandPathAndMacros(programParamsConfigurator, value, runConfig) },
            )
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
