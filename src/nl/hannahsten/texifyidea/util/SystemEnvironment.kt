package nl.hannahsten.texifyidea.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessNotCreatedException
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

        val isPerlInstalled: Boolean by lazy {
            "perl -v".runCommand()?.contains("This is perl") == true
        }

        val isTexcountAvailable: Boolean by lazy {
            "texcount".runCommand()?.contains("TeXcount") == true
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

fun Process.getOutput() = inputStream.bufferedReader().readText().trim() + errorStream.bufferedReader().readText().trim()

/**
 * See [runCommand], but also returns exit code.
 *
 * @param killAfterTimeout If true, process will be killed after timeout. If false, just return output.
 */
fun runCommandWithExitCode(vararg commands: String, workingDirectory: File? = null, timeout: Long = 3, killAfterTimeout: Boolean = true): Pair<String?, Int> {
    return try {
        val proc = GeneralCommandLine(*commands)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(workingDirectory)
            .createProcess()

        if (proc.waitFor(timeout, TimeUnit.SECONDS)) {
            Pair(proc.getOutput(), proc.exitValue())
        }
        else {
            // todo find a way to get output of alive process
            var output = ""
            var exitValue = 0
            if (killAfterTimeout) {
                proc.destroy()
                proc.waitFor()
                // At this point, the inputStream is finished so we can safely get the output without blocking
                output = proc.getOutput()
                exitValue = proc.exitValue()
            }
            Pair(output, exitValue)
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
