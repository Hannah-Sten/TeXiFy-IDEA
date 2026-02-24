package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.BibtexStepOptions

internal class BibtexRunStep(
    private val stepConfig: BibtexStepOptions,
) : LatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val workingDirectory = CommandLineRunStep.resolveWorkingDirectory(context, stepConfig.workingDirectoryPath)
        val command = mutableListOf(stepConfig.compilerPath ?: stepConfig.bibliographyCompiler.executableName)
        if (stepConfig.bibliographyCompiler.name == "BIBER") {
            command += "--input-directory=${workingDirectory?.toString() ?: context.mainFile.parent.path}"
        }
        stepConfig.compilerArguments
            ?.takeIf(String::isNotBlank)
            ?.let { command += ParametersListUtil.parse(it) }
        command += context.mainFile.nameWithoutExtension

        val configurator = ProgramParametersConfigurator()

        return createCompilationHandler(
            environment = context.environment,
            mainFile = context.mainFile,
            command = command,
            workingDirectory = workingDirectory,
            expandMacrosEnvVariables = context.runConfig.expandMacrosEnvVariables,
            envs = context.runConfig.environmentVariables.envs,
            expandEnvValue = { value -> configurator.expandPathAndMacros(value, null, context.runConfig.project) ?: value },
        )
    }
}
