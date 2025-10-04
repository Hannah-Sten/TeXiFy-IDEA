package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.TeXception
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.errors.NoReply
import org.freedesktop.dbus.errors.ServiceUnknown
import org.freedesktop.dbus.exceptions.DBusException
import org.freedesktop.dbus.types.UInt32
import org.gnome.evince.Daemon
import org.gnome.evince.SyncViewSourcePointStruct
import org.gnome.evince.Window

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

    override fun openFile(pdfPath: String, project: Project, newWindow: Boolean, focusAllowed: Boolean, forceRefresh: Boolean) {
        // Opening the file when not already open will lose focus, so we don't want to do that. However, we have to, otherwise manual forward search will not work
        openFile(pdfPath, project)
    }

    /**
     * Open a file in Evince, starting it if it is not running yet. This also finds the process owner of the pdf, so we can execute forward search later.
     */
    fun openFile(pdfFilePath: String, project: Project) {
        // Will do nothing if file is already open in Evince
        findProcessOwner(pdfFilePath, project)
    }

    // This is not really correct since Evince will always focus on forward search, but we still want to show the option to users so that they can use it to disable forward search if they don't want to lose focus after compilation.
    override val isFocusSupported = true

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