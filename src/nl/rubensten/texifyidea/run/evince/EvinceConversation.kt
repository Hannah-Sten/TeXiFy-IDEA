package nl.rubensten.texifyidea.run.evince

import com.intellij.openapi.util.SystemInfo
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.gnome.evince.Daemon
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Indicates whether Evince is installed and available.
 *
 * todo check if this doesn't hurt performance too much (otherwise can be computed on start and saved, see sumatra)
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
    val evinceDaemonPath = "/org/gnome/evince/Daemon"

    /**
     * Object name of the Evince daemon.
     */
    val evinceDaemonName = "org.gnome.evince.Daemon"

    /**
     * Open a file in Evince, starting it if it is not running yet.
     */
    fun openFile(pdfFilePath: String, newWindow: Boolean = false, focus: Boolean = false, forceRefresh: Boolean = false) {
        // todo what happens when the command fails?
        // todo possibly use the default document viewer if evince not available?
        // todo is filepath full path or relative? need working dir in second case
        // todo focus (always on top) or not depends on DE? https://unix.stackexchange.com/questions/36998/open-pdf-previewer-width-specific-size-and-position-and-always-on-top-from-com
        // Will do nothing if file is already open in Evince
        "evince $pdfFilePath".runCommand()
    }

    /**
     * Execute forward search, highlighting a certain line in Evince.
     * Will also open Evince on the given pdf file.
     *
     * @param pdfFilePath Full path to a pdf file.
     * @param sourceFilePath Full path to the LaTeX source file.
     * @param line Line number in the source file to highlight in the pdf.
     */
    fun forwardSearch(pdfFilePath: String? = null, sourceFilePath: String, line: Int) {
        // Initialize a session bus
        val connection = DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION)

        // Get the Daemon object using its bus name and object path
        val daemon = connection.getRemoteObject(evinceDaemonName, evinceDaemonPath, Daemon::class.java)

        // If no pdf file is given, try to guess it
        val pdfFile = pdfFilePath ?: sourceFilePath.dropLast(4) + ".pdf"

        // Call the method on the D-Bus by using the function we defined in the Daemon interface
        val documentProcessOwner = daemon.FindDocument("file://$pdfFile", true)

        // Theoretically we should use the Java D-Bus bindings as well to call SyncView, but that did
        // not succeed with a NoReply exception, so we will execute a command via the shell.
        val command = "gdbus call --session --dest $documentProcessOwner --object-path /org/gnome/evince/Window/0 --method org.gnome.evince.Window.SyncView $sourceFilePath '($line, 1)' 0"
        Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
    }
}
