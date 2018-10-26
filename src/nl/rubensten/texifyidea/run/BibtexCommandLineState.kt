package nl.rubensten.texifyidea.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.roots.ProjectRootManager

/**
 * @author Sten Wessel
 */
open class BibtexCommandLineState(
        environment: ExecutionEnvironment,
        private val runConfig: BibtexRunConfiguration
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val rootManager = ProjectRootManager.getInstance(environment.project)
        val fileIndex = rootManager.fileIndex
        val moduleRoot = fileIndex.getContentRootForFile(runConfig.mainFile!!)

        val compiler = runConfig.compiler ?: throw ExecutionException("No valid compiler specified.")
        val command: List<String> = compiler.getCommand(runConfig, environment.project) ?: throw ExecutionException("Compile command could not be created.")

        // The working directory is as specified by the user in the working directory.
        // The fallback (if null) directory is the directory of the main file.
        val commandLine = GeneralCommandLine(command).withWorkDirectory(runConfig.bibWorkingDir?.path ?: runConfig.mainFile?.parent?.path)

        val handler: ProcessHandler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }
}
