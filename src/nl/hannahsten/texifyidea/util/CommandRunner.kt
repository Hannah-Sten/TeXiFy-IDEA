package nl.hannahsten.texifyidea.util

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.util.io.awaitExit
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.nio.file.Path
import javax.swing.SwingUtilities

data class CommandResult(
    val exitCode: Int,
    val standardOutput: String?,
    val errorOutput: String?
) {

    val output: String?
        get() = if (standardOutput != null || errorOutput != null) (standardOutput ?: "") + (errorOutput ?: "") else null
}

/**
 * Run a command in the terminal in a non-blocking way.
 *
 * @param workingDirectory If provided, the process' working directory.
 * @param input If provided, this will be written to the process' input pipe.
 * @param discardOutput Whether to discard all command outputs (stdout, stderr) and only return its exit code.
 * @param returnExceptionMessageAsErrorOutput Whether to return exception messages as error output if exceptions are thrown.
 * @param timeout The timeout for execution. Does not stop reading the process' output as long as it is available.
 */
suspend fun runCommandNonBlocking(
    vararg commands: String,
    workingDirectory: File? = null,
    input: String? = null,
    discardOutput: Boolean = false,
    returnExceptionMessageAsErrorOutput: Boolean = false,
    timeout: Long = 3
): CommandResult = withContext(Dispatchers.IO) {
    try {
        Log.debug("isEDT=${SwingUtilities.isEventDispatchThread()} Executing in ${workingDirectory ?: "current working directory"} ${GeneralCommandLine(*commands).commandLineString}")

        // where/which commands occur often but do not change since the output depends on PATH, so can be cached
        val isExecutableLocationCommand = commands.size == 2 && listOf("where", "which").contains(commands[0])
        if (isExecutableLocationCommand && SystemEnvironment.executableLocationCache[commands[1]] != null) {
            val standardOutput = SystemEnvironment.executableLocationCache[commands[1]]
            Log.debug("Retrieved output of $commands from cache: $standardOutput")
            return@withContext CommandResult(0, standardOutput, null)
        }

        val processBuilder = GeneralCommandLine(*commands)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(workingDirectory)
            .toProcessBuilder()

        if (discardOutput) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
        }

        val process = processBuilder.start()

        // Closing the outputWriter, inputReador or errorReader also closes the socket which would make other readers or writers fail, so make sure to close after we are done
        val outputWriter = process.outputWriter()
        val inputReader = process.inputReader()
        val errorReader = process.errorReader()

        try {
            if (input != null) outputWriter.write(input)
            val output = if (!discardOutput) async { inputReader.readText() } else null
            val error = if (!discardOutput) async { errorReader.readText() } else null

            // Make sure the await() is done within the timeout, otherwise the timeout will not work, see example in #3921
            val result = withTimeoutOrNull(1_000 * timeout) {
                CommandResult(process.awaitExit(), output?.await()?.trim(), error?.await()?.trim())
            } ?: run {
                process.destroy()
                Log.debug("${commands.firstOrNull()} destroyed after timeout $timeout seconds")
                CommandResult(-1, null, "Process destroyed after timeout $timeout seconds")
            }

            Log.debug("${commands.firstOrNull()} exited with ${result.exitCode} ${result.standardOutput?.take(100)} ${result.errorOutput?.take(100)}")

            // Update cache of where/which output
            if (isExecutableLocationCommand) {
                SystemEnvironment.executableLocationCache[commands[1]] = result.standardOutput
            }

            return@withContext result
        }
        finally {
            outputWriter.close()
            inputReader.close()
            errorReader.close()
        }
    }
    catch (e: IOException) {
        Log.debug(e.message ?: "Unknown IOException occurred")

        return@withContext CommandResult(
            -1,
            null,
            if (returnExceptionMessageAsErrorOutput) e.message else null
        )
    }
    catch (e: ExecutionException) {
        Log.debug(e.message ?: "Unknown ExecutionException occurred")

        return@withContext CommandResult(
            -1,
            null,
            if (returnExceptionMessageAsErrorOutput) e.message else null
        )
    }
}


suspend fun sendCommandNonBlocking(
    vararg commands: String,
    workingDirectory: Path? = null,
    timeout: Long = 3
) : Int {
    val processBuilder = GeneralCommandLine(*commands).withWorkingDirectory(workingDirectory)
        .toProcessBuilder()
    processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
    processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
    val process = processBuilder.start()
    val exitCode = withTimeoutOrNull(1_000 * timeout) {
        withContext(Dispatchers.IO) {
            process.awaitExit()
        }
    } ?: run {
        process.destroy()
        Log.debug("${commands.firstOrNull()} destroyed after timeout $timeout seconds")
        -1
    }
    return exitCode
}

/**
 * Run a command in the terminal.
 *
 * @return The output of the command or null if an exception was thrown.
 */
fun runCommand(vararg commands: String, workingDirectory: File? = null, timeout: Long = 3): String? =
    runBlocking {
        runCommandNonBlocking(*commands, workingDirectory = workingDirectory, timeout = timeout).output
    }

/**
 * See [runCommandNonBlocking].
 *
 * @param returnExceptionMessage Whether to return exception messages as output if exceptions are thrown.
 * @param inputString If provided, this will be written to the process' input pipe.
 * @return Pair of output (stdout + stderr) to exit code.
 */
fun runCommandWithExitCode(
    vararg commands: String,
    workingDirectory: File? = null,
    timeout: Long = 3,
    returnExceptionMessage: Boolean = false,
    discardOutput: Boolean = false,
    inputString: String = ""
): Pair<String?, Int> =
    runBlocking {
        with(
            runCommandNonBlocking(
                *commands,
                workingDirectory = workingDirectory,
                timeout = timeout,
                returnExceptionMessageAsErrorOutput = returnExceptionMessage,
                discardOutput = discardOutput,
                input = inputString
            )
        ) {
            Pair(output, exitCode)
        }
    }