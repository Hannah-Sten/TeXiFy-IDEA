package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import java.nio.file.Path

internal class CommandLineRunStep(
    override val id: String,
    private val commandLineSupplier: (LatexRunStepContext) -> String,
    private val workingDirectorySupplier: (LatexRunStepContext) -> Path? = { defaultWorkingDirectory(it) },
) : LatexRunStep {

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler {
        val runConfig = context.runConfig
        val commandLine = commandLineSupplier(context)
        val command = CommandLineRunStepParser.parse(commandLine)
        if (command.isEmpty()) {
            throw ExecutionException("Step $id has an empty command line.")
        }
        val configurator = ProgramParametersConfigurator()

        return createCompilationHandler(
            environment = context.environment,
            mainFile = context.mainFile,
            command = command,
            workingDirectory = workingDirectorySupplier(context),
            expandMacrosEnvVariables = runConfig.expandMacrosEnvVariables,
            envs = runConfig.environmentVariables.envs,
            expandEnvValue = { value -> configurator.expandPathAndMacros(value, null, runConfig.project) ?: value },
        )
    }

    companion object {

        fun defaultWorkingDirectory(context: LatexRunStepContext): Path? = context.executionState.resolvedAuxDir?.let { Path.of(it.path) }
            ?: context.executionState.resolvedOutputDir?.let { Path.of(it.path) }
            ?: context.executionState.resolvedWorkingDirectory
            ?: Path.of(context.mainFile.parent.path)
    }
}
