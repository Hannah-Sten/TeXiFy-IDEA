package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.MakeindexStepConfig
import nl.hannahsten.texifyidea.util.appendExtension

internal class MakeindexRunStep(
    private val stepConfig: MakeindexStepConfig,
) : LatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val command = mutableListOf(stepConfig.program.executableName)
        stepConfig.commandLineArguments
            ?.takeIf(String::isNotBlank)
            ?.let { command += ParametersListUtil.parse(it) }
        command += context.mainFile.nameWithoutExtension.appendExtension("idx")

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
