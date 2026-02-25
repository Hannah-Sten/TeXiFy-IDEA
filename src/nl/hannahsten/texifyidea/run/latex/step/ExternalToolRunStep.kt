package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.ExternalToolStepOptions

internal class ExternalToolRunStep(
    private val stepConfig: ExternalToolStepOptions,
) : ProcessLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val executable = stepConfig.executable?.trim()
        val command = if (executable.isNullOrBlank()) {
            emptyList()
        }
        else {
            buildList {
                add(executable)
                stepConfig.arguments
                    ?.takeIf(String::isNotBlank)
                    ?.let { addAll(ParametersListUtil.parse(it)) }
            }
        }
        if (command.isEmpty()) {
            throw ExecutionException("External tool step has an empty executable.")
        }

        val configurator = ProgramParametersConfigurator()
        return createCompilationHandler(
            environment = context.environment,
            mainFile = context.mainFile,
            command = command,
            workingDirectory = CommandLineRunStep.resolveWorkingDirectory(context, stepConfig.workingDirectoryPath),
            expandMacrosEnvVariables = context.runConfig.expandMacrosEnvVariables,
            envs = context.runConfig.environmentVariables.envs,
            expandEnvValue = { value -> configurator.expandPathAndMacros(value, null, context.runConfig.project) ?: value },
        )
    }
}
