package nl.rubensten.texifyidea.run

import com.intellij.openapi.util.SystemInfo
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Indicates whether Evince is installed and available.
 *
 * todo check if this doesn't hurt performance too much (otherwise can be computed on start and saved, see sumatra)
 */
fun isEvinceAvailable(): Boolean {
    // Only support Evince on Linux, although it can be installed on other systems like Mac
    if(!SystemInfo.isLinux) {
        return false
    }

    // Find out whether Evince is installed and in PATH, otherwise we can't use it
    val output = "which evince".runCommand()
    println(output)
    return output?.contains("evince") ?: false
}

/**
 * Run a command in the terminal.
 *
 * @return The output of the command or null if an exception was thrown.
 */
fun String.runCommand(): String? {
    return try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        // Timeout value
        proc.waitFor(10, TimeUnit.SECONDS)
        proc.inputStream.bufferedReader().readText()
    } catch(e: IOException) {
        e.printStackTrace()
        null
    }
}
