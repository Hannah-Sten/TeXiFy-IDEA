package nl.hannahsten.texifyidea.run.makeindex

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.PackageUtils.PACKAGE_COMMANDS
import nl.hannahsten.texifyidea.util.PackageUtils.getIncludedPackages
import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Run makeindex.
 */
class MakeindexCommandLineState(
        environment: ExecutionEnvironment,
        private val runConfig: LatexRunConfiguration
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val mainFile = runConfig.mainFile ?: throw ExecutionException("Main file to compile is not found or missing.")
        val workDir = runConfig.getAuxilDirectory()

        // Find index package options
        val mainPsiFile = runConfig.mainFile?.psiFile(environment.project) ?: throw ExecutionException("Main psifile not found")
        val indexPackageOptions = LatexCommandsIndex.getItemsInFileSet(mainPsiFile)
                .filter { it.commandToken.text in PACKAGE_COMMANDS }
                .filter { command -> command.requiredParameters.any { it == "imakeidx"}}
                .map { it.optionalParameters }
                .flatten()

        val indexProgram = if (indexPackageOptions.contains("xindy")) "texindy" else "makeindex"

        val command = listOf(indexProgram, mainFile.nameWithoutExtension.appendExtension("idx"))
        val commandLine = GeneralCommandLine(command).withWorkDirectory(workDir?.path)

        val handler: ProcessHandler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }
}