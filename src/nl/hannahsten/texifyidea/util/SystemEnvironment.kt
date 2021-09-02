package nl.hannahsten.texifyidea.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessNotCreatedException
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
