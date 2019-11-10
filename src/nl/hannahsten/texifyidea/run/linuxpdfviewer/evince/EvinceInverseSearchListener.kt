package nl.hannahsten.texifyidea.run.linuxpdfviewer.evince

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
class EvinceInverseSearchListener {

    /**
     * Starts a listener which listens for inverse search actions from Evince.
     */
    fun start() {
        // Run in a coroutine so the main thread can continue
        // If the program finishes, the listener will stop as well
        GlobalScope.launch {
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

            while (line != null) {
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
                    val lineNumberString = lineNumberLine.substring(lineNumberLine.indexOf("int32") + 6, lineNumberLine.indexOf("int32") + 7)
                    val lineNumber = Integer.parseInt(lineNumberString)

                    // Sync the IDE
                    syncSource(filename, lineNumber)
                }

                line = bufferedReader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Sync the IDE on the given source file and line number.
     *
     * @param filePath   Full to a file.
     * @param lineNumber Line number in the file.
     */
    private fun syncSource(filePath: String, lineNumber: Int) {
        val path = PathManager.getBinPath()
        val name = ApplicationNamesInfo.getInstance().scriptName

        val command = "$path/$name.sh --line $lineNumber \"$filePath\""

        try {
            Runtime.getRuntime().exec(command)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}