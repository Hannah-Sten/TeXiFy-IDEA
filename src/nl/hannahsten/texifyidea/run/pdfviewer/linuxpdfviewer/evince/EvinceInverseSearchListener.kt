package nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.evince

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import nl.hannahsten.texifyidea.util.runCommand
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Listen on the D-Bus for inverse search signals coming from Evince.
 *
 * For debugging, the self-contained project at https://github.com/PHPirates/evince_dbus may be helpful.
 *
 * @author Thomas Schouten
 */
object EvinceInverseSearchListener {

    private var currentCoroutineJob: Job? = null

    /**
     * Starts a listener which listens for inverse search actions from Evince.
     */
    fun start(project: Project) {
        // Check if Evince version supports dbus
        // Technically only version 2.32 is needed, but since 3.0 was released back
        // in 2011 we just check for major version 3, much easier

        try {
            // Assumes version will be given in the format GNOME Document Viewer 3.34.2
            val majorVersion = "evince --version".runCommand()
                ?.split(" ")
                ?.lastOrNull()
                ?.split(".")
                ?.firstOrNull()
                ?.toInt()
            if (majorVersion != null && majorVersion < 3) {
                Notification("LaTeX", "Old Evince version found", "Please update Evince to at least version 3 to use forward/backward search", NotificationType.ERROR).notify(project)
                return
            }
        }
        catch (ignored: NumberFormatException) {}

        // Run in a coroutine so the main thread can continue
        // If the program finishes, the listener will stop as well
        currentCoroutineJob = GlobalScope.launch {
            startListening()
        }
    }

    /**
     * Start listening for backward search calls on the D-Bus.
     */
    private fun startListening() {
        try {
            // Listen on the session D-Bus to catch SyncSource signals emitted by Evince
            val commands = arrayOf("dbus-monitor", "--session")
            val process = Runtime.getRuntime().exec(commands)
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = bufferedReader.readLine()

            while (line != null && currentCoroutineJob?.isActive == true) {
                // Check if a SyncSource signal appeared from Evince and if so, read the contents
                if (line.contains("interface=org.gnome.evince.Window; member=SyncSource")) {
                    // Get the value between quotes
                    val filenameLine = bufferedReader.readLine()
                    var filename = filenameLine.substring(filenameLine.indexOf("\"") + 1, filenameLine.lastIndexOf("\""))
                    filename = filename.replaceFirst("file://".toRegex(), "")

                    // Pass over the "struct {" line
                    bufferedReader.readLine()

                    // Get the location represented by the struct
                    val lineNumberLine = bufferedReader.readLine()
                    val lineNumberString = lineNumberLine.substring(lineNumberLine.indexOf("int32") + 6, lineNumberLine.length).trim()
                    val lineNumber = Integer.parseInt(lineNumberString)

                    // Sync the IDE
                    syncSource(filename, lineNumber)
                }

                // Check whether we would block before doing a blocking readLine call
                // This is to ensure we can quickly stop this coroutine on plugin unload
                while (!bufferedReader.ready()) {
                    Thread.sleep(100)
                    if (currentCoroutineJob?.isActive == false) return
                }
                line = bufferedReader.readLine()
            }
        }
        catch (e: IOException) {
            e.printStackTrace()
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

        val command = "$path/$name.sh --line $lineNumber \"$filePath\""

        try {
            Runtime.getRuntime().exec(command)
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun unload() {
        currentCoroutineJob?.cancelAndJoin()
    }
}