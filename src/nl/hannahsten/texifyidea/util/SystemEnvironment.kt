package nl.hannahsten.texifyidea.util

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
 * Consider using GeneralCommandLine instead.
 *
 * @return The output of the command or null if an exception was thrown.
 */
fun runCommand(vararg commands: String, workingDirectory: File? = null): String? {
    return try {
        val command = arrayListOf(*commands)
        val proc = ProcessBuilder(command)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .directory(workingDirectory)
            .start()

        if (proc.waitFor(3, TimeUnit.SECONDS)) {
            proc.inputStream.bufferedReader().readText().trim() + proc.errorStream.bufferedReader().readText().trim()
        }
        else {
            proc.destroy()
            proc.waitFor()
            null
        }
    }
    catch (e: IOException) {
        null // Don't print the stacktrace because that is confusing.
    }
}