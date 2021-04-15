package nl.hannahsten.texifyidea.run.legacy.makeindex

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.legacy.MakeindexProgram
import nl.hannahsten.texifyidea.util.appendExtension

/**
 * Run makeindex.
 */
class MakeindexCommandLineState(
    environment: ExecutionEnvironment,
    private val mainFile: VirtualFile?,
    private val workingDirectory: VirtualFile?,
    private val makeindexOptions: HashMap<String, String>,
    private val indexProgram: MakeindexProgram
) : CommandLineState(environment) {

    @Throws(ExecutionException::class)
    override fun startProcess(): ProcessHandler {
        if (mainFile == null) {
            throw ExecutionException("Main file to compile is not found or missing.")
        }

        val indexBasename = makeindexOptions.getOrDefault("name", mainFile.nameWithoutExtension)

        // texindy requires the file extension
        val indexFilename = if (indexProgram != MakeindexProgram.XINDY) indexBasename else indexBasename.appendExtension("idx")

        val command = listOf(indexProgram.executableName, indexFilename)
        val commandLine = GeneralCommandLine(command).withWorkDirectory(workingDirectory?.path)

        val handler: ProcessHandler = KillableProcessHandler(commandLine)

        // Reports exit code to run output window when command is terminated
        ProcessTerminatedListener.attach(handler, environment.project)

        return handler
    }
}