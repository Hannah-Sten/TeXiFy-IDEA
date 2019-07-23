package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.roots.ProjectRootManager
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

class MakeindexCommandLineState(
        environment: ExecutionEnvironment,
        private val makeindexRunConfig: MakeindexRunConfiguration
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val project = environment.project
        val runConfig = makeindexRunConfig.latexRunConfig?.configuration as? LatexRunConfiguration ?: throw ExecutionException("Cannot get LaTeX run configuration.")
        val mainFile = runConfig.mainFile ?: throw ExecutionException("Main file to compile is not found or missing.")

        fun findChild(name: String) = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile)?.findChild(name)

        // Find working directory
        val workDir = when {
            runConfig.hasAuxiliaryDirectories -> findChild("auxil")
            runConfig.hasOutputDirectories -> findChild("out")
            else -> findChild(mainFile.parent.name)
        }

        val command = listOf("makeindex", mainFile.nameWithoutExtension)
        val commandLine = GeneralCommandLine(command).withWorkDirectory(workDir.toString())

        val handler: ProcessHandler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }
}