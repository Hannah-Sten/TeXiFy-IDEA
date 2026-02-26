package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexSessionInitializer

internal class LatexCompileRunStep(
    private val stepConfig: LatexCompileStepOptions,
) : ProcessLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val session = context.session
        LatexSessionInitializer.updateOutputFilePath(session, stepConfig)

        val command = stepConfig.compiler.buildCommand(session, stepConfig)
        val programParamsConfigurator = ProgramParametersConfigurator()

        return createCompilationHandler(
            environment = context.environment,
            mainFile = session.mainFile,
            command = command,
            workingDirectory = session.workingDirectory,
            expandMacrosEnvVariables = context.runConfig.expandMacrosEnvVariables,
            envs = context.runConfig.environmentVariables.envs,
            expandEnvValue = { value ->
                programParamsConfigurator.expandPathAndMacros(value, null, context.runConfig.project) ?: value
            },
        )
    }
}
