package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

class MakeindexCommandLineState(
        environment: ExecutionEnvironment,
        private val runConfig: LatexRunConfiguration
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val mainFile = runConfig.mainFile ?: throw ExecutionException("Main file to compile is not found or missing.")
        val workDir = runConfig.getAuxilDirectory()

        val command = listOf("makeindex", mainFile.nameWithoutExtension)
        val commandLine = GeneralCommandLine(command).withWorkDirectory(workDir?.path)

        val handler: ProcessHandler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }
}