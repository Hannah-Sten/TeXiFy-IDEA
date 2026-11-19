package nl.hannahsten.texifyidea.util

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.util.io.awaitExit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.IOException
import java.nio.file.Path
import javax.swing.SwingUtilities
import kotlin.time.Duration.Companion.milliseconds

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
    workingDirectory: Path? = null,
    input: String? = null,
    discardOutput: Boolean = false,
    returnExceptionMessageAsErrorOutput: Boolean = false,
    timeout: Long = 3
): CommandResult = withContext(Dispatchers.IO) {
    try {
        Log.debug("isEDT=${SwingUtilities.isEventDispatchThread()} Executing in ${workingDirectory ?: "current working directory"} ${GeneralCommandLine(*commands).commandLineString}")

        val (_, cachedResult) = getCachedResult(commands)

        if (cachedResult != null) return@withContext cachedResult

        val processBuilder = GeneralCommandLine(*commands)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .apply { if (workingDirectory != null) withWorkingDirectory(workingDirectory) }
            .toProcessBuilder()

        if (discardOutput) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
        }

        val process = processBuilder.start()

        // Closing the outputWriter, inputReader or errorReader also closes the socket which would make other readers or writers fail, so make sure to close after we are done
        val outputWriter = process.outputWriter()
        val inputReader = process.inputReader()
        val errorReader = process.errorReader()

        try {
            if (input != null) outputWriter.write(input)
            val output = if (!discardOutput) async { readTextIgnoreClosedStream(inputReader) } else null
            val error = if (!discardOutput) async { readTextIgnoreClosedStream(errorReader) } else null

            // Make sure the await() is done within the timeout, otherwise the timeout will not work, see example in #3921
            val result = withTimeoutOrNull((1_000 * timeout).milliseconds) {
                CommandResult(process.awaitExit(), output?.await()?.trim(), error?.await()?.trim())
            } ?: run {
                process.destroy()
                Log.debug("${commands.firstOrNull()} destroyed after timeout $timeout seconds")
                CommandResult(-1, null, "Process destroyed after timeout $timeout seconds")
            }

            Log.debug("${commands.firstOrNull()} exited with ${result.exitCode} ${result.standardOutput?.take(100)} ${result.errorOutput?.take(100)}")

            // Update cache of where/which/readlink output
            updateCache(commands, result)

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

        val result = CommandResult(
            -1,
            null,
            if (returnExceptionMessageAsErrorOutput) e.message else null
        )
        updateCache(commands, result)
        return@withContext result
    }
    catch (e: ExecutionException) {
        Log.debug(e.message ?: "Unknown ExecutionException occurred")

        val result = CommandResult(
            -1,
            null,
            if (returnExceptionMessageAsErrorOutput) e.message else null
        )
        updateCache(commands, result)
        return@withContext result
    }
}

/**
 * Execute command synchronously without coroutine dispatcher switching to avoid thread starvation in blocking contexts.
 *
 * This implementation intentionally duplicates process execution and stream handling logic from [runCommandNonBlocking]
 * to maximize system reliability and overall utility across differing execution contexts:
 * - Calling [runCommandNonBlocking] from synchronous code via `runBlocking` introduces severe deadlock and thread
 *   starvation risks when invoked from background worker threads (e.g., during indexing or inspections) that may
 *   hold IntelliJ Platform read locks while awaiting `Dispatchers.IO` threads.
 * - Conversely, delegating [runCommandNonBlocking] to this synchronous method via `withContext(Dispatchers.IO)` would
 *   block physical dispatcher threads during I/O and process waiting, sacrificing cooperative coroutine cancellation
 *   and non-blocking suspension provided by [com.intellij.util.io.awaitExit] and [withTimeoutOrNull].
 *
 * Maintaining two specialized implementations yields the greatest net benefit by ensuring deadlock-free synchronous
 * execution on caller threads while preserving optimal non-blocking resource utilization for coroutine callers.
 */
fun runCommandSync(
    vararg commands: String,
    workingDirectory: Path? = null,
    input: String? = null,
    discardOutput: Boolean = false,
    returnExceptionMessageAsErrorOutput: Boolean = false,
    timeout: Long = 3
): CommandResult {
    val (_, cachedResult) = getCachedResult(commands)
    if (cachedResult != null) return cachedResult

    return try {
        Log.debug("isEDT=${SwingUtilities.isEventDispatchThread()} Executing sync in ${workingDirectory ?: "current working directory"} ${GeneralCommandLine(*commands).commandLineString}")

        val processBuilder = GeneralCommandLine(*commands)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .apply { if (workingDirectory != null) withWorkingDirectory(workingDirectory) }
            .toProcessBuilder()

        if (discardOutput) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
        }

        val process = processBuilder.start()

        val outputWriter = process.outputWriter()
        val inputReader = process.inputReader()
        val errorReader = process.errorReader()

        try {
            if (input != null) {
                outputWriter.write(input)
                outputWriter.flush()
            }
            outputWriter.close()

            var output: String? = null
            var error: String? = null

            val outputThread = if (!discardOutput) {
                Thread({
                    output = readTextIgnoreClosedStream(inputReader)
                }, "CommandRunner-sync-stdout").apply {
                    isDaemon = true
                    start()
                }
            }
            else null

            val errorThread = if (!discardOutput) {
                Thread({
                    error = readTextIgnoreClosedStream(errorReader)
                }, "CommandRunner-sync-stderr").apply {
                    isDaemon = true
                    start()
                }
            }
            else null

            val finished = process.waitFor(timeout, java.util.concurrent.TimeUnit.SECONDS)
            val result = if (finished) {
                outputThread?.join(1000)
                errorThread?.join(1000)
                CommandResult(process.exitValue(), output?.trim(), error?.trim())
            }
            else {
                process.destroyForcibly()
                Log.debug("${commands.firstOrNull()} destroyed after timeout $timeout seconds")
                CommandResult(-1, null, "Process destroyed after timeout $timeout seconds")
            }

            Log.debug("${commands.firstOrNull()} exited with ${result.exitCode} ${result.standardOutput?.take(100)} ${result.errorOutput?.take(100)}")

            updateCache(commands, result)

            result
        }
        finally {
            try {
                outputWriter.close()
            } catch (_: Exception) {}
            try {
                inputReader.close()
            } catch (_: Exception) {}
            try {
                errorReader.close()
            } catch (_: Exception) {}
        }
    }
    catch (e: IOException) {
        Log.debug(e.message ?: "Unknown IOException occurred")
        val result = CommandResult(-1, null, if (returnExceptionMessageAsErrorOutput) e.message else null)
        updateCache(commands, result)
        result
    }
    catch (e: ExecutionException) {
        Log.debug(e.message ?: "Unknown ExecutionException occurred")
        val result = CommandResult(-1, null, if (returnExceptionMessageAsErrorOutput) e.message else null)
        updateCache(commands, result)
        result
    }
}

private fun getCacheKey(commands: Array<out String>): String? {
    if (commands.size == 2 && listOf("where", "which").contains(commands[0]) && commands.getOrNull(1).isNullOrBlank().not()) {
        return commands[1]
    }
    if (commands.size == 3 && commands[0] == "readlink" && commands[1] == "-f" && commands.getOrNull(2).isNullOrBlank().not()) {
        return "readlink:" + commands[2]
    }
    return null
}

/**
 * where/which/readlink commands occur often but do not change since the output depends on PATH/filesystem, so can be cached
 */
private fun getCachedResult(commands: Array<out String>): Pair<Boolean, CommandResult?> {
    val cacheKey = getCacheKey(commands)
    if (cacheKey != null) {
        val cachedExecutableLocation = SystemEnvironment.executableLocationCache[cacheKey]
        if (cachedExecutableLocation != null) {
            Log.debug("Retrieved output of ${commands.joinToString(" ")} from cache: $cachedExecutableLocation")
            val exitCode = if (cachedExecutableLocation.isEmpty()) 1 else 0
            val stdOutput = cachedExecutableLocation.ifEmpty { null }
            return Pair(true, CommandResult(exitCode, stdOutput, null))
        }
        return Pair(true, null)
    }
    return Pair(false, null)
}

private fun updateCache(commands: Array<out String>, result: CommandResult) {
    val cacheKey = getCacheKey(commands)
    if (cacheKey != null) {
        val value = if (result.exitCode == 0) (result.standardOutput ?: "") else ""
        SystemEnvironment.executableLocationCache[cacheKey] = value
    }
}

private fun readTextIgnoreClosedStream(reader: BufferedReader): String? = try {
    reader.readText()
}
catch (e: IOException) {
    // Not sure how this can happen (#4062)
    if (e.message?.contains("Stream closed") == true) {
        Log.info("Ignored closed stream: " + e.message)
        e.message
    }
    else throw e
}

/**
 * Run a command in the terminal.
 *
 * @return The output of the command or null if an exception was thrown.
 */
fun runCommand(vararg commands: String, workingDirectory: Path? = null, timeout: Long = 3): String? =
    runCommandSync(*commands, workingDirectory = workingDirectory, timeout = timeout).output

/**
 * See [runCommandNonBlocking].
 *
 * @param returnExceptionMessage Whether to return exception messages as output if exceptions are thrown.
 * @param inputString If provided, this will be written to the process' input pipe.
 * @return Pair of output (stdout + stderr) to exit code.
 */
fun runCommandWithExitCode(
    vararg commands: String,
    workingDirectory: Path? = null,
    timeout: Long = 3,
    returnExceptionMessage: Boolean = false,
    discardOutput: Boolean = false,
    inputString: String = ""
): Pair<String?, Int> {
    val result = runCommandSync(
        *commands,
        workingDirectory = workingDirectory,
        timeout = timeout,
        returnExceptionMessageAsErrorOutput = returnExceptionMessage,
        discardOutput = discardOutput,
        input = inputString.ifEmpty { null }
    )
    return Pair(result.output, result.exitCode)
}