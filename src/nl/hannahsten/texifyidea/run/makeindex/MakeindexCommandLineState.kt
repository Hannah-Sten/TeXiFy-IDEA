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
import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.util.files.psiFile

/**
 * Run makeindex.
 */
class MakeindexCommandLineState(
        environment: ExecutionEnvironment,
        private val runConfig: LatexRunConfiguration,
        private val indexPackageOptions: List<String>,
        private val makeindexOptions: HashMap<String, String>
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        val mainFile = runConfig.mainFile ?: throw ExecutionException("Main file to compile is not found or missing.")
        val workDir = runConfig.getAuxilDirectory()

        var indexProgram = if (indexPackageOptions.contains("xindy")) "texindy" else "makeindex"

        // Possible extra settings to override the indexProgram, see the imakeidx docs
        for (program in listOf("makeindex", "xindy", "texindy", "truexindy")) {
            if (makeindexOptions.contains("program")) {
                indexProgram = makeindexOptions["program"] ?: break
                if (indexProgram == "xindy") {
                    indexProgram = "texindy"
                }
                if (indexProgram == "truexindy") {
                    indexProgram = "xindy"
                }
            }
        }

        val indexFilename = makeindexOptions.getOrDefault("name", mainFile.nameWithoutExtension).appendExtension("idx")

        val command = listOf(indexProgram, indexFilename)
        val commandLine = GeneralCommandLine(command).withWorkDirectory(workDir?.path)

        val handler: ProcessHandler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }
}