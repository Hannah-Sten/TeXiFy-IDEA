package nl.hannahsten.texifyidea.run.linuxpdfviewer

import java.io.IOException
import java.util.concurrent.TimeUnit

abstract class ViewerConversation {
    abstract fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int)
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
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}