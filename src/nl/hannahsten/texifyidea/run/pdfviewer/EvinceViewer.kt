package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.TexifyCoroutine
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.errors.NoReply
import org.freedesktop.dbus.errors.ServiceUnknown
import org.freedesktop.dbus.exceptions.DBusException
import org.freedesktop.dbus.types.UInt32
import org.gnome.evince.Daemon
import org.gnome.evince.SyncViewSourcePointStruct
import org.gnome.evince.Window
import java.io.IOException

/**
 * Send commands to Evince.
 * For more information about D-Bus and forward/inverse search, see https://github.com/PHPirates/evince_dbus
 *
 * @author Thomas Schouten
 */
object EvinceViewer : SystemPdfViewer("Evince", "evince") {

    /**
     * Object path of the Evince daemon. Together with the object name, this allows us to find the
     * D-Bus object which allows us to execute the FindDocument function, which is exported on the D-Bus
     * by Evince.
     */
    private const val EVINCE_DAEMON_PATH = "/org/gnome/evince/Daemon"

    /**
     * Object name of the Evince daemon.
     */
    private const val EVINCE_DAEMON_NAME = "org.gnome.evince.Daemon"

    /**
     * Object path of the Evince daemon. Together with the object name, this allows us to find the
     * D-Bus object which allows us to execute the FindDocument function, which is exported on the D-Bus
     * by Evince.
     */
    private const val EVINCE_WINDOW_PATH = "/org/gnome/evince/Window/0"

    /**
     * This variable will hold the latest known Evince process owner. We need to know the owner of the pdf file in order to execute forward search.
     */
    private var processOwner: String? = null

    /**
     * Open a file in Evince, starting it if it is not running yet. This also finds the process owner of the pdf, so we can execute forward search later.
     */
    fun openFile(pdfFilePath: String, project: Project) {
        // Will do nothing if file is already open in Evince
        findProcessOwner(pdfFilePath, project)
    }

    override val isFocusSupported: Boolean
        get() = false

    /**
     * Execute forward search, highlighting a certain line in Evince.
     * If a pdf file is given, it will execute FindDocument and open the pdf file again to find the latest process owner. If the pdf file is already open, this will do nothing.
     *
     * @param outputPath Full path to a pdf file.
     * @param sourceFilePath Full path to the LaTeX source file.
     * @param line Line number in the source file to highlight in the pdf.
     */
    override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        // If we are not allowed to change focus, we cannot open the pdf or do forward search because this will always change focus with Evince
        if (!focusAllowed) {
            return
        }

        if (outputPath != null) {
            findProcessOwner(outputPath, project)
        }

        if (processOwner != null) {
            try {
                // Get DBusConnection
                DBusConnectionBuilder.forSessionBus().build().use { connection ->
                    // Get the Object corresponding to the interface and call the functions to the processOwner
                    val window = connection.getRemoteObject(processOwner, EVINCE_WINDOW_PATH, Window::class.java)

                    // Sync the Evince view to the current position
                    try {
                        window.SyncView(sourceFilePath, SyncViewSourcePointStruct(line, -1), UInt32(0))
                    }
                    catch (ignored: NoReply) {}
                    catch (e: ServiceUnknown) {
                        Notification("LaTeX", "Cannot sync position to Evince", "Please update Evince and then try again.", NotificationType.ERROR).notify(project)
                    }
                }
            }
            catch (e: DBusException) {
                Notification("LaTeX", "Cannot sync position to Evince", "The Connection could not be established.", NotificationType.ERROR).notify(project)
            }
        }
        else {
            // If the user used the forward search menu action
            if (outputPath == null) {
                Notification("LaTeX", "Could not execute forward search", "Please make sure you have compiled the document first, and that your path does not contain spaces.", NotificationType.ERROR).notify(project)
            }
            else {
                throw TeXception("Could not execute forward search with Evince because something went wrong when finding the pdf file at $outputPath")
            }
        }
    }

    /**
     * Execute FindDocument on the D-Bus in order to find the process owner of the given pdf file, i.e. the process name which we can use for
     * forward/inverse search.
     * The value found will be saved for later use.
     *
     * @param pdfFilePath Full path to the pdf file to find the owner of.
     */
    private fun findProcessOwner(pdfFilePath: String, project: Project) {
        try {
            // Get DBusConnection
            DBusConnectionBuilder.forSessionBus().build().use { connection ->
                // Get the Daemon object using its bus name and object path
                val daemon = connection.getRemoteObject(EVINCE_DAEMON_NAME, EVINCE_DAEMON_PATH, Daemon::class.java)

                // Call the method on the D-Bus by using the function we defined in the Daemon interface
                // Catch a NoReply, because it is unknown why Evince cannot start so we don't try to fix that
                try {
                    processOwner = daemon.FindDocument("file://$pdfFilePath", true)
                }
                catch (ignored: NoReply) {}
                catch (e: ServiceUnknown) {
                    Notification("LaTeX", "Cannot communicate to Evince", "Please update Evince and then try again.", NotificationType.ERROR).notify(project)
                }
            }
        }
        catch (e: DBusException) {
            Notification("LaTeX", "Cannot communicate to Evince", "The connection could not be established.", NotificationType.ERROR).notify(project)
        }
    }
}

/**
 * Listen on the D-Bus for inverse search signals coming from Evince.
 *
 * For debugging, the self-contained project at https://github.com/PHPirates/evince_dbus may be helpful.
 *
 * @author Thomas Schouten
 */
object EvinceInverseSearchListener {

    private val currentCoroutineScope: CoroutineScope
        get() = TexifyCoroutine.getInstance().coroutineScope

    private var sessionConnection: DBusConnection? = null

    private var syncSourceHandler: AutoCloseable? = null

    /**
     * Starts a listener which listens for inverse search actions from Evince.
     */
    fun start(project: Project) {
        // Check if Evince version supports dbus
        // The exact version required is not know, but 3.28 works and 3.0.2 does not (#2087), even though dbus is supported since 2.32
        if (SystemEnvironment.evinceVersion.majorVersion <= 3 && SystemEnvironment.evinceVersion.minorVersion <= 28) {
            Notification("LaTeX", "Old Evince version found", "Please update Evince to at least version 3.28 to use forward/backward search", NotificationType.ERROR).notify(project)
            return
        }

        if (sessionConnection == null) {
            // Create session connection here, so that if we're running tests this can fail silently
            try {
                sessionConnection = DBusConnectionBuilder.forSessionBus().build()
            }
            catch (e: Exception) {
                Notification("LaTeX", "Cannot get connection to DBus", "Check if the correct packages are installed", NotificationType.ERROR).notify(project)
                return
            }
        }

        // Check if we already have a listener
        if (syncSourceHandler != null) {
            return
        }

        // Run in a coroutine so the main thread can continue
        // If the program finishes, the listener will stop as well
        currentCoroutineScope.launch {
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
        currentCoroutineScope.cancel(CancellationException(("Unloading the plugin")))
    }
}