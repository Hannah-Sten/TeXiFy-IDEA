package nl.rubensten.texifyidea.run.evince

import com.intellij.openapi.util.SystemInfo
import nl.rubensten.texifyidea.TeXception
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.errors.NoReply
import org.gnome.evince.Daemon
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Indicates whether Evince is installed and available.
 *
 * For debugging, the self-contained project at https://github.com/PHPirates/evince_dbus may be helpful.
 */
fun isEvinceAvailable(): Boolean {
    // Only support Evince on Linux, although it can be installed on other systems like Mac
    if (!SystemInfo.isLinux) {
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
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

/**
 * Send commands to Evince.
 * For more information about D-Bus and forward/inverse search, see https://github.com/PHPirates/evince_dbus
 *
 * @author Thomas Schouten
 */
object EvinceConversation {

    /**
     * Object path of the Evince daemon. Together with the object name, this allows us to find the
     * D-Bus object which allows us to execute the FindDocument function, which is exported on the D-Bus
     * by Evince.
     */
    private const val evinceDaemonPath = "/org/gnome/evince/Daemon"

    /**
     * Object name of the Evince daemon.
     */
    private const val evinceDaemonName = "org.gnome.evince.Daemon"

    /**
     * This variable will hold the latest known Evince process owner. We need to know the owner of the pdf file in order to execute forward search.
     */
    private var processOwner: String? = null

    /**
     * Open a file in Evince, starting it if it is not running yet. This also finds the process owner of the pdf, so we can execute forward search later.
     */
    fun openFile(pdfFilePath: String) {
        // Will do nothing if file is already open in Evince
        findProcessOwner(pdfFilePath)
    }

    /**
     * Execute forward search, highlighting a certain line in Evince.
     * If a pdf file is given, it will execute FindDocument and open the pdf file again to find the latest process owner. If the pdf file is already open, this will do nothing.
     *
     * @param pdfFilePath Full path to a pdf file.
     * @param sourceFilePath Full path to the LaTeX source file.
     * @param line Line number in the source file to highlight in the pdf.
     */
    fun forwardSearch(pdfFilePath: String? = null, sourceFilePath: String, line: Int) {
        if (pdfFilePath != null) {
            findProcessOwner(pdfFilePath)
        }

        if (processOwner != null) {
            // Theoretically we should use the Java D-Bus bindings as well to call SyncView, but that did
            // not succeed with a NoReply exception, so we will execute a command via the shell
            val command = "gdbus call --session --dest $processOwner --object-path /org/gnome/evince/Window/0 --method org.gnome.evince.Window.SyncView $sourceFilePath '($line, 1)' 0"
            Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
        }
        else {
            throw TeXception("Could not execute forward search with Evince because something went wrong when finding the pdf file.")
        }
    }

    /**
     * Execute FindDocument on the D-Bus in order to find the process owner of the given pdf file, i.e. the process name which we can use for
     * forward/inverse search.
     * The value found will be saved for later use.
     *
     * @param pdfFilePath Full path to the pdf file to find the owner of.
     */
    private fun findProcessOwner(pdfFilePath: String) {
        // Initialize a session bus
        val connection = DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION)

        // Get the Daemon object using its bus name and object path
        val daemon = connection.getRemoteObject(evinceDaemonName, evinceDaemonPath, Daemon::class.java)

        // Call the method on the D-Bus by using the function we defined in the Daemon interface
        // Catch a NoReply, because it is unknown why Evince cannot start so we don't try to fix that
        try {
            processOwner = daemon.FindDocument("file://$pdfFilePath", true)
        } catch (ignored: NoReply) {}
    }

}
