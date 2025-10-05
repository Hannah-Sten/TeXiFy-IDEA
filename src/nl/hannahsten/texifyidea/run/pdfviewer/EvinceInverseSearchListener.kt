package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.delay
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.TexifyCoroutine
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.gnome.evince.Window
import java.io.IOException

/**
 * Listen on the D-Bus for inverse search signals coming from Evince.
 *
 * For debugging, the self-contained project at https://github.com/PHPirates/evince_dbus may be helpful.
 *
 * @author Thomas Schouten
 */
object EvinceInverseSearchListener {

    private var sessionConnection: DBusConnection? = null

    private var syncSourceHandler: AutoCloseable? = null

    /**
     * Starts a listener which listens for inverse search actions from Evince.
     */
    fun start(project: Project) {
        // Check if Evince version supports dbus
        // The exact version required is not know, but 3.28 works and 3.0.2 does not (#2087), even though dbus is supported since 2.32
        if (SystemEnvironment.evinceVersion.majorVersion <= 3 && SystemEnvironment.evinceVersion.minorVersion <= 28) {
            Notification(
                "LaTeX",
                "Old Evince version found",
                "Please update Evince to at least version 3.28 to use forward/backward search",
                NotificationType.ERROR
            ).notify(project)
            return
        }

        if (sessionConnection == null) {
            // Create session connection here, so that if we're running tests this can fail silently
            try {
                sessionConnection = DBusConnectionBuilder.forSessionBus().build()
            }
            catch (e: Exception) {
                Notification(
                    "LaTeX",
                    "Cannot get connection to DBus",
                    "Check if the correct packages are installed",
                    NotificationType.ERROR
                ).notify(project)
                return
            }
        }

        // Check if we already have a listener
        if (syncSourceHandler != null) {
            return
        }

        // Run in a coroutine so the main thread can continue
        // If the program finishes, the listener will stop as well
        TexifyCoroutine.runInBackground {
            // Delay execution and hope everything is ready (#3995)
            delay(1000)
            try {
                startListening()
            }
            catch (e: Exception) {
                // See e.g. #3955, #4030, let's try again
                sessionConnection?.register()
                startListening()
            }
        }
    }

    /**
     * Start listening for backward search calls on the D-Bus.
     */
    private fun startListening() {
        Log.debug("Starting Evince inverse search listener")
        syncSourceHandler = sessionConnection?.addSigHandler(Window.SyncSource::class.java) { signal ->
            val filename = signal.sourceFile.replaceFirst("file://".toRegex(), "")
            syncSource(filename, signal.sourcePoint.line)
        }
    }

    /**
     * Sync the IDE on the given source file and line number.
     *
     * @param filePath Full to a file.
     * @param lineNumber Line number in the file.
     */
    private fun syncSource(filePath: String, lineNumber: Int) {
        val path = PathManager.getBinPath()
        val name = ApplicationNamesInfo.getInstance().scriptName

        val command = arrayOf("$path/$name.sh", "--line", lineNumber.toString(), "\"$filePath\"")

        try {
            Runtime.getRuntime().exec(command)
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun unload() {
        // Remove the listener
        syncSourceHandler?.close()
        // Properly close the connection
        sessionConnection?.close()
    }
}