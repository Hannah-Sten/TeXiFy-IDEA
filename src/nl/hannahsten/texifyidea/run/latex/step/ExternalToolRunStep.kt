package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.ExternalToolStepOptions

internal class ExternalToolRunStep(
    private val stepConfig: ExternalToolStepOptions,
) : LatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val commandLine = stepConfig.commandLine?.trim()
            ?: throw ExecutionException("External tool step has an empty command line.")
        val command = CommandLineRunStepParser.parse(commandLine)
        if (command.isEmpty()) {
            throw ExecutionException("External tool step has an empty command line.")
        }

        val configurator = ProgramParametersConfigurator()
        return createCompilationHandler(
            environment = context.environment,
            mainFile = context.mainFile,
            command = command,
            workingDirectory = CommandLineRunStep.defaultWorkingDirectory(context),
            expandMacrosEnvVariables = context.runConfig.expandMacrosEnvVariables,
            envs = context.runConfig.environmentVariables.envs,
            expandEnvValue = { value -> configurator.expandPathAndMacros(value, null, context.runConfig.project) ?: value },
        )
    }
}
