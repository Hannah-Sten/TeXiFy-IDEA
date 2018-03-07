package nl.rubensten.texifyidea.run

import com.intellij.openapi.util.SystemInfo
import com.pretty_tools.dde.client.DDEClientConversation
import nl.rubensten.texifyidea.TeXception

/**
 * Send commands to SumatraPDF.
 *
 * This is available on Windows only.
 *
 * @author Sten Wessel
 * @since b0.4
 */
object SumatraConversation {

    /**
     * Indicates whether SumatraPDF is installed and DDE communication is enabled.
     *
     * Is computed once at initialization (for performance), which means that the IDE needs to be restarted when users
     * install SumatraPDF while running TeXiFy.
     */
    @JvmField val isAvailable: Boolean

    private val server = "SUMATRA"
    private val topic = "control"
    private val conversation: DDEClientConversation?

    init {
        if (!SystemInfo.isWindows || !sumatraInstalled()) {
            conversation = null
            isAvailable = false
        }
        else {
            try {
                conversation = DDEClientConversation()
            }
            catch (e: NoClassDefFoundError) {
                isAvailable = false
                throw TeXception("Native library DLLs could not be found.", e)
            }

            isAvailable = true
        }

    }

    fun openFile(pdfFilePath: String, newWindow: Boolean = false, focus: Boolean = false, forceRefresh: Boolean = false, start: Boolean = false) {
        if (start) {
            Runtime.getRuntime().exec("cmd.exe /c start SumatraPDF -reuse-instance \"$pdfFilePath\"")
        }
        else {
            execute("Open(\"$pdfFilePath\", ${newWindow.bit}, ${focus.bit}, ${forceRefresh.bit})")
        }
    }

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
            conversation.execute(commands.joinToString(separator = "") { "[$it]" })
        }
        catch (e: Exception) {
            throw TeXception("Connection to SumatraPDF was disrupted.", e)
        }
        finally {
            conversation?.disconnect()
        }
    }

    private fun sumatraInstalled(): Boolean {
        // Look up SumatraPDF registry key
        val process = Runtime.getRuntime().exec(
                "reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\SumatraPDF.exe\" /ve"
        )

        val br = process.inputStream.bufferedReader()
        val firstLine = br.readLine() ?: return false
        br.close()

        return !firstLine.startsWith("ERROR:")
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
