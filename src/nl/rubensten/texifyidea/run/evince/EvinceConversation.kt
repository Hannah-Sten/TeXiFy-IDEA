package nl.rubensten.texifyidea.run.evince

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

/**
 * Send commands to Evince.
 *
 * @author Thomas Schouten
 */
object EvinceConversation {
    fun openFile(pdfFilePath: String, newWindow: Boolean = false, focus: Boolean = false, forceRefresh: Boolean = false) {
        // todo what happens when the command fails?
        // todo possibly use the default document viewer if evince not available?
//        try {
            // todo is filepath full path or relative? need working dir in second case
            // todo focus (always on top) or not depends on DE? https://unix.stackexchange.com/questions/36998/open-pdf-previewer-width-specific-size-and-position-and-always-on-top-from-com
            "evince $pdfFilePath".runCommand()
//            SumatraConversation.execute("Open(\"$pdfFilePath\", ${newWindow.bit}, ${focus.bit}, ${forceRefresh.bit})")
//        }
//        catch (e: TeXception) {
//            Runtime.getRuntime().exec("cmd.exe /c start SumatraPDF -reuse-instance \"$pdfFilePath\"")
//        }
    }

    fun forwardSearch(pdfFilePath: String? = null, sourceFilePath: String, line: Int) {

    }
}
