package nl.hannahsten.texifyidea.util

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
    }


}

fun runCommand(vararg commands: String): String {
    try {
        val command = arrayListOf(*commands)
        val proc = ProcessBuilder(command)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        // Timeout value
        proc.waitFor(3, TimeUnit.SECONDS)
        return proc.inputStream.bufferedReader().readText()
    }
    catch (e: IOException) {
        e.printStackTrace()
    }
    return ""
}