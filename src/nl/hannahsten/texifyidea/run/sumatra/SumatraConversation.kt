package nl.hannahsten.texifyidea.run.sumatra

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.pretty_tools.dde.client.DDEClientConversation
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.linuxpdfviewer.ViewerConversation
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import nl.hannahsten.texifyidea.util.runCommand

/**
 * Indicates whether SumatraPDF is installed and DDE communication is enabled.
 *
 * Is computed once at initialization (for performance), which means that the IDE needs to be restarted when users
 * install SumatraPDF while running TeXiFy.
 */
val isSumatraAvailable: Boolean by lazy {
    if (!SystemInfo.isWindows || !isSumatraInstalled()) return@lazy false

    // Try if native bindings are available
    try {
        DDEClientConversation()
    }
    catch (e: UnsatisfiedLinkError) {
        Log.info("Native library DLLs could not be found.")
        return@lazy false
    }
    catch (e: NoClassDefFoundError) {
        Log.info("Native library DLLs could not be found.")
        return@lazy false
    }

    true
}

private fun isSumatraInstalled(): Boolean {
    // Try some SumatraPDF registry keys
    // For some reason this first one isn't always present anymore, it used to be
    val regQuery1 = runCommand("reg", "query", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\SumatraPDF.exe", "/ve")?.startsWith("ERROR:") == false
    val regQuery2 = runCommand("reg", "query", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\SumatraPDF.pdf", "/ve")?.startsWith("ERROR:") == false

    if (regQuery1 || regQuery2) return true

    // Try if Sumatra is in PATH
    return runCommandWithExitCode("start", "SumatraPDF").second == 0
}

/**
 * Send commands to SumatraPDF.
 *
 * This is available on Windows only.
 *
 * @author Sten Wessel
 * @since b0.4
 */
class SumatraConversation : ViewerConversation() {

    private val server = "SUMATRA"
    private val topic = "control"
    private var conversation: DDEClientConversation? = null

    init {
        if (isSumatraAvailable) {
            try {
                conversation = DDEClientConversation()
            }
            catch (e: NoClassDefFoundError) {
                throw TeXception("Native library DLLs could not be found.", e)
            }
        }
    }

    /**
     * Open a file in SumatraPDF, starting it if it is not running yet.
     */
    fun openFile(pdfFilePath: String, newWindow: Boolean = false, focus: Boolean = false, forceRefresh: Boolean = false, sumatraPath: String? = null) {
        try {
            execute("Open(\"$pdfFilePath\", ${newWindow.bit}, ${focus.bit}, ${forceRefresh.bit})")
        }
        catch (e: TeXception) {
            // In case the user provided a custom path to SumatraPDF, add it to the path before executing
            val processBuilder = ProcessBuilder("cmd.exe", "/C", "start", "SumatraPDF", "-reuse-instance", pdfFilePath)
            if (sumatraPath != null) {
                processBuilder.environment()["Path"] = sumatraPath
            }
            processBuilder.start()
        }
    }

    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        forwardSearch(pdfPath, sourceFilePath, line, focus = focusAllowed)
    }

    /**
     * Execute forward search, highlighting a certain line in SumatraPDF.
     */
    fun forwardSearch(pdfFilePath: String? = null, sourceFilePath: String, line: Int, newWindow: Boolean = false, focus: Boolean = false) {
        val pdfPath = if (pdfFilePath != null) "\"$pdfFilePath\", " else ""
        execute("ForwardSearch($pdfPath\"$sourceFilePath\", $line, 0, ${newWindow.bit}, ${focus.bit})")
    }

    fun gotoNamedDest(pdfFilePath: String, destinationName: String) {
        execute("GotoNamedDest(\"$pdfFilePath\", \"$destinationName\")")
    }

    fun gotoPage(pdfFilePath: String, page: Int) {
        execute("GotoPage(\"$pdfFilePath\", $page)")
    }

    fun setView(pdfFilePath: String, viewMode: ViewMode, zoomLevel: ZoomLevel, scroll: Pair<Int, Int>? = null) {
        val s = if (scroll != null) ", ${scroll.first}, ${scroll.second}" else ""
        execute("SetView(\"$pdfFilePath\", \"${viewMode.description}\", ${zoomLevel.percentage}$s)")
    }

    private fun execute(vararg commands: String) {
        try {
            conversation!!.connect(server, topic)
            conversation!!.execute(commands.joinToString(separator = "") { "[$it]" })
        }
        catch (e: Exception) {
            throw TeXception("Connection to SumatraPDF was disrupted.", e)
        }
        finally {
            conversation?.disconnect()
        }
    }

    /**
     * @author Sten Wessel
     */
    enum class ViewMode(val description: String) {

        SINGLE_PAGE("single page"),
        FACING("facing"),
        BOOK_VIEW("book view"),
        CONTINUOUS("continuous"),
        CONTINUOUS_FACING("continuous facing"),
        CONTINUOUS_BOOK_VIEW("continuous book view");
    }

    /**
     * @author Sten Wessel
     */
    class ZoomLevel(val percentage: Int) {

        companion object {

            private val fitPage = ZoomLevel(-1)
            private val fitWidth = ZoomLevel(-2)
            private val fitContent = ZoomLevel(-3)
        }

        init {
            require(percentage in 8..6400) { "Percentage must be in range [8,6400]" }
        }
    }
}

private val Boolean.bit: Int
    get() = if (this) 1 else 0
