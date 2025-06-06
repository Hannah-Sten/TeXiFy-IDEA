package nl.hannahsten.texifyidea.run.legacy.bibtex

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Companion.toWslPathIfNeeded
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.SystemEnvironment
import kotlin.io.path.Path

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
        val compilerCommand = compiler.getCommand(runConfig, environment.project)?.toMutableList() ?: throw ExecutionException("Compile command could not be created.")

        // See LatexCompiler#getCommand
        val command = if (runConfig.getLatexDistributionType() == LatexDistributionType.WSL_TEXLIVE) {
            var wslCommand = GeneralCommandLine(compilerCommand).commandLineString

            // Custom compiler arguments specified by the user
            runConfig.compilerArguments?.let { arguments ->
                ParametersListUtil.parse(arguments)
                    .forEach { wslCommand += " $it" }
            }

            wslCommand += " ${runConfig.bibWorkingDir?.path?.toWslPathIfNeeded(runConfig.getLatexDistributionType())}"

            mutableListOf(*SystemEnvironment.wslCommand, wslCommand)
        }
        else {
            compilerCommand
        }

        // The working directory is as specified by the user in the working directory.
        // The fallback (if null or empty) directory is the directory of the main file.
        val bibPath = runConfig.bibWorkingDir?.path
        val mainPath = runConfig.mainFile?.parent?.path
        val commandLine = if (!bibPath.isNullOrBlank()) {
            GeneralCommandLine(command).withWorkingDirectory(Path((bibPath)))
        }
        else if (!mainPath.isNullOrBlank()) {
            GeneralCommandLine(command).withWorkingDirectory(Path(mainPath))
        }
        else {
            throw ExecutionException("No working directory specified for BibTeX run configuration.")
        }

        val handler: ProcessHandler = KillableProcessHandler(commandLine.withEnvironment(runConfig.environmentVariables.envs))

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }
}
