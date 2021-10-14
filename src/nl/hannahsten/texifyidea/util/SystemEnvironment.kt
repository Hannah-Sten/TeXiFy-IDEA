package nl.hannahsten.texifyidea.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessNotCreatedException
import com.intellij.openapi.util.SystemInfo
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

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

            val isAvailable = if (SystemInfo.isUnix) {
                "command -v $command".runCommandWithExitCode().second == 0
            }
            else {
                "where $command".runCommandWithExitCode().second == 0
            }
            availabilityCache[command] = isAvailable
            return isAvailable
        }

        // Assumes version will be given in the format GNOME Document Viewer 3.34.2
        val evinceVersion: DefaultArtifactVersion by lazy {
            DefaultArtifactVersion("evince --version".runCommand()?.split(" ")?.lastOrNull())
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

/**
 * See [runCommand], but also returns exit code.
 */
fun runCommandWithExitCode(vararg commands: String, workingDirectory: File? = null, timeout: Long = 3): Pair<String?, Int> {
    return try {
        val proc = GeneralCommandLine(*commands)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(workingDirectory)
            .createProcess()

        if (proc.waitFor(timeout, TimeUnit.SECONDS)) {
            val output = proc.inputStream.bufferedReader().readText().trim() + proc.errorStream.bufferedReader().readText().trim()
            return Pair(output, proc.exitValue())
        }
        else {
            val output = proc.inputStream.bufferedReader().readText().trim() + proc.errorStream.bufferedReader().readText().trim()
            proc.destroy()
            proc.waitFor()
            Pair(output, proc.exitValue())
        }
    }
    catch (e: IOException) {
        Pair(null, -1) // Don't print the stacktrace because that is confusing.
    }
    catch (e: ProcessNotCreatedException) {
        // e.g. if the command is just trying if a program can be run or not, and it's not the case
        Pair(null, -1)
    }
}
