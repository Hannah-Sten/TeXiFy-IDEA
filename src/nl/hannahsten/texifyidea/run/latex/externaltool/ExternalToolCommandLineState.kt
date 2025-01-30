package nl.hannahsten.texifyidea.run.latex.externaltool

import com.intellij.execution.ExecutionException
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.compiler.ExternalTool

/**
 * Run an external tool.
 */
class ExternalToolCommandLineState(
    environment: ExecutionEnvironment,
    private val mainFile: VirtualFile?,
    private val workingDirectory: VirtualFile?,
    private val tool: ExternalTool,
    private val environmentVariables: EnvironmentVariablesData
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        if (mainFile == null) {
            throw ExecutionException("Main file to compile is not found or missing.")
        }

        val command = listOf(tool.executableName, mainFile.nameWithoutExtension)
        val commandLine = GeneralCommandLine(command).withWorkDirectory(workingDirectory?.path)
            .withEnvironment(environmentVariables.envs)

        val handler: ProcessHandler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }
}