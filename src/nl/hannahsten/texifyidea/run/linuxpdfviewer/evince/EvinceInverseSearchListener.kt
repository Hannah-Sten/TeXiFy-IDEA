package nl.hannahsten.texifyidea.run.linuxpdfviewer.evince

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import nl.hannahsten.texifyidea.util.SystemEnvironment
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

    private var currentCoroutineScope = MainScope()

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

        // Run in a coroutine so the main thread can continue
        // If the program finishes, the listener will stop as well
        currentCoroutineScope.launch {
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

            while (line != null && currentCoroutineScope.isActive) {
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
                    if (!currentCoroutineScope.isActive) return
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

    fun unload() {
        currentCoroutineScope.cancel(CancellationException(("Unloading the plugin")))
    }
}