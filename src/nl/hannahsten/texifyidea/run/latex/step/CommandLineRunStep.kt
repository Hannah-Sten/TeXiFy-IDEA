package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.util.ProgramParametersConfigurator
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import java.nio.file.Path

internal class CommandLineRunStep(
    override val configId: String,
    override val id: String,
    private val commandLineSupplier: (LatexRunStepContext) -> String,
    private val workingDirectorySupplier: (LatexRunStepContext) -> Path? = { defaultWorkingDirectory(it) },
) : ProcessLatexRunStep {

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
            mainFile = context.session.mainFile,
            command = command,
            workingDirectory = workingDirectorySupplier(context),
            expandMacrosEnvVariables = runConfig.expandMacrosEnvVariables,
            envs = runConfig.environmentVariables.envs,
            expandEnvValue = { value -> configurator.expandPathAndMacros(value, null, runConfig.project) ?: value },
        )
    }

    companion object {

        fun resolveWorkingDirectory(context: LatexRunStepContext, configuredPath: String?): Path? {
            val configured = configuredPath
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?.let(::pathOrNull)
                ?.let { LatexPathResolver.resolve(it, context.session.mainFile, context.environment.project) }
            return configured ?: defaultWorkingDirectory(context)
        }

        fun defaultWorkingDirectory(context: LatexRunStepContext): Path? = context.session.auxDir?.let { Path.of(it.path) }
            ?: context.session.outputDir?.let { Path.of(it.path) }
            ?: context.session.workingDirectory
            ?: Path.of(context.session.mainFile.parent.path)
    }
}
