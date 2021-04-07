package nl.hannahsten.texifyidea.run.bibtex

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment

/**
 * @author Sten Wessel
 */
open class BibtexCommandLineState(
    environment: ExecutionEnvironment,
    private val runConfig: BibtexRunConfiguration
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {

        val compiler = runConfig.compiler ?: throw ExecutionException("No valid compiler specified.")
        val command: List<String> = compiler.getCommand(runConfig, environment.project) ?: throw ExecutionException("Compile command could not be created.")

        // The working directory is as specified by the user in the working directory.
        // The fallback (if null or empty) directory is the directory of the main file.
        val bibPath = runConfig.bibWorkingDir?.path
        val commandLine = if (!bibPath.isNullOrBlank()) {
            GeneralCommandLine(command).withWorkDirectory(bibPath)
        }
        else GeneralCommandLine(command).withWorkDirectory(runConfig.mainFile?.parent?.path)

        val handler: ProcessHandler = KillableProcessHandler(commandLine.withEnvironment(runConfig.environmentVariables.envs))

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }
}
