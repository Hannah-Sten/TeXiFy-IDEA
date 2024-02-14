package nl.hannahsten.texifyidea.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessNotCreatedException
import com.intellij.openapi.util.SystemInfo
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

/**
 * Information about the system other than the LatexDistribution or the OS.
 */
class SystemEnvironment {

    companion object {

        val inkscapeMajorVersion: Int by lazy {
            "inkscape --version".runCommand()
                ?.split(" ")?.getOrNull(1)
                ?.split(".")?.firstOrNull()
                ?.toInt() ?: 0
        }

        val isInkscapeInstalledAsSnap: Boolean by lazy {
            "snap list".runCommand()?.contains("inkscape") == true
        }

        /** Cache for [isAvailable]. */
        private var availabilityCache = mutableMapOf<String, Boolean>()

        /**
         * Check if [command] is available as a system command.
         */
        fun isAvailable(command: String): Boolean {
            // Not thread-safe, but don't think that's a problem here
            availabilityCache.getOrDefault(command, null)?.let { return it }

            // Has to be run with bash because command is a shell command
            val isAvailable = if (SystemInfo.isUnix) {
                val (_, exitCode) = runCommandWithExitCode("bash", "-c", "command -v $command")
                exitCode == 0
            }
            else {
                "where $command".runCommandWithExitCode().second == 0
            }
            availabilityCache[command] = isAvailable
            return isAvailable
        }

        // Assumes version will be given in the format GNOME Document Viewer 3.34.2
        val evinceVersion: DefaultArtifactVersion by lazy {
            DefaultArtifactVersion("evince --version".runCommand()?.split(" ")?.lastOrNull() ?: "")
        }
    }
}

/**
 * Run a command in the terminal.
 *
 * @return The output of the command or null if an exception was thrown.
 */
fun runCommand(vararg commands: String, workingDirectory: File? = null): String? {
    return runCommandWithExitCode(*commands, workingDirectory = workingDirectory).first
}

fun Process.getOutput() = inputStream.bufferedReader().readText().trim() + errorStream.bufferedReader().readText().trim()

/**
 * See [runCommand], but also returns exit code.
 *
 * @param killAfterTimeout If true, process will be killed after timeout. If false, just return output.
 * @param returnExceptionMessage Whether to return exception messages if exceptions are thrown.
 * @param nonBlocking If true, the function will not block waiting for output
 * @param inputString If provided, this will be written to the process outputStream before starting the process.
 */
fun runCommandWithExitCode(vararg commands: String, workingDirectory: File? = null, timeout: Long = 3, killAfterTimeout: Boolean = true, returnExceptionMessage: Boolean = false, nonBlocking: Boolean = false, inputString: String = ""): Pair<String?, Int> {
    Log.debug("isEDT=${SwingUtilities.isEventDispatchThread()} Executing in ${workingDirectory ?: "current working directory"} ${GeneralCommandLine(*commands).commandLineString}")
    return try {
        val proc = GeneralCommandLine(*commands)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(workingDirectory)
            .createProcess()

        if (inputString.isNotBlank()) {
            proc.outputStream.bufferedWriter().apply {
                write(inputString)
                close()
            }
        }

        if (proc.waitFor(timeout, TimeUnit.SECONDS)) {
            val output = readInputStream(nonBlocking, proc)
            Log.debug("${commands.firstOrNull()} exited with ${proc.exitValue()} ${output.take(100)}")
            Pair(output, proc.exitValue())
        }
        else {
            // todo find a way to get output of alive process
            var output = ""
            // If the program has timed out, something is stuck so we are not going to wait until it prints its stdout/stderr, we just check if ready and otherwise are out of luck
            if (proc.inputStream.bufferedReader().ready()) {
                output += proc.inputStream.bufferedReader().readText().trim()
            }
            if (proc.errorStream.bufferedReader().ready()) {
                output += proc.errorStream.bufferedReader().readText().trim()
            }
            if (killAfterTimeout) {
                proc.destroy()
                proc.waitFor()
                // At this point, the inputStream is finished so we can safely get the output without blocking
                if (!nonBlocking) {
                    output = proc.getOutput()
                }
                Log.debug("${commands.firstOrNull()} exited ${proc.exitValue()} with timeout")
            }
            Pair(output, proc.exitValue())
        }
    }
    catch (e: IOException) {
        Log.debug(e.message ?: "Unknown IOException occurred")
        if (!returnExceptionMessage) {
            Pair(null, -1) // Don't print the stacktrace because that is confusing.
        }
        else {
            Pair(e.message, -1)
        }
    }
    catch (e: ProcessNotCreatedException) {
        Log.debug(e.message ?: "Unknown ProcessNotCreatedException occurred")
        // e.g. if the command is just trying if a program can be run or not, and it's not the case
        if (!returnExceptionMessage) {
            Pair(null, -1)
        }
        else {
            Pair(e.message, -1)
        }
    }
}

/**
 * Read input and error streams. If non-blocking, we will skip reading the streams if they are not ready.
 */
private fun readInputStream(nonBlocking: Boolean, proc: Process): String {
    var output = ""
    if (nonBlocking) {
        if (proc.inputStream.bufferedReader().ready()) {
            output += proc.inputStream.bufferedReader().readText().trim()
        }
        if (proc.errorStream.bufferedReader().ready()) {
            output += proc.errorStream.bufferedReader().readText().trim()
        }
    }
    else {
        output = proc.inputStream.bufferedReader().readText().trim() + proc.errorStream.bufferedReader().readText().trim()
    }
    return output
}
