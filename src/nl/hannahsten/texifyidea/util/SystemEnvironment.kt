package nl.hannahsten.texifyidea.util

import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Information about the system other than the LatexDistribution or the OS.
 */
class SystemEnvironment {

    companion object {

        val inkscapeVersion: String by lazy {
            runCommand("inkscape", "--version").split(" ").getOrNull(1) ?: ""
        }

        val isInkscapeInstalledAsSnap: Boolean by lazy {
            runCommand("snap", "list").contains("inkscape")
        }
    }


}

internal fun runCommand(vararg commands: String): String {
    try {
        val command = arrayListOf(*commands)
        val proc = ProcessBuilder(command)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        // Timeout value
        proc.waitFor(10, TimeUnit.SECONDS)
        return proc.inputStream.bufferedReader().readText()
    }
    catch (e: IOException) {
        e.printStackTrace()
    }
    return ""
}