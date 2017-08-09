package nl.rubensten.texifyidea.run

import com.intellij.openapi.vfs.VirtualFile
import com.pretty_tools.dde.client.DDEClientConversation
import nl.rubensten.texifyidea.TeXception

/**
 *
 * @author Sten Wessel
 */
object SumatraConversation {
    private val SERVER = "SUMATRA"
    private val TOPIC = "control"

    val conversation: DDEClientConversation = DDEClientConversation()

    fun openFile(pdfFile: VirtualFile, newWindow: Boolean = false, focus: Boolean = false, forceRefresh: Boolean = false) {
        execute("Open(\"${pdfFile.path}\", ${newWindow.bit}, ${focus.bit}, ${forceRefresh.bit})")
    }

    fun forwardSearch(pdfFile: VirtualFile? = null, sourceFile: VirtualFile, line: Int, newWindow: Boolean = false, focus: Boolean = false) {
        val pdfPath = if (pdfFile != null) "\"${pdfFile.path}\", " else ""
        execute("ForwardSearch($pdfPath\"${sourceFile.path}\", $line, 0, ${newWindow.bit}, ${focus.bit})")
    }

    fun gotoNamedDest(pdfFile: VirtualFile, destinationName: String) {
        execute("GotoNamedDest(\"${pdfFile.path}\", \"$destinationName\")")
    }

    fun gotoPage(pdfFile: VirtualFile, page: Int) {
        execute("GotoPage(\"${pdfFile.path}\", $page)")
    }

    fun setView(pdfFile: VirtualFile, viewMode: ViewMode, zoomLevel: ZoomLevel, scroll: Pair<Int, Int>? = null) {
        val s = if (scroll != null) ", ${scroll.first}, ${scroll.second}" else ""
        execute("SetView(\"${pdfFile.path}\", \"${viewMode.description}\", ${zoomLevel.percentage}$s)")
    }

    private fun execute(vararg commands: String) {
        try {
            conversation.connect(SERVER, TOPIC)
            conversation.execute(commands.joinToString(separator = "") { "[$it]" })
        } catch (e: Exception) {
            throw TeXception("Connection to SumatraPDF was disrupted.")
        } finally {
            conversation.disconnect()
        }
    }

    enum class ViewMode(val description: String) {
        SINGLE_PAGE("single page"),
        FACING("facing"),
        BOOK_VIEW("book view"),
        CONTINUOUS("continuous"),
        CONTINUOUS_FACING("continuous facing"),
        CONTINUOUS_BOOK_VIEW("continuous book view");
    }

    class ZoomLevel(val percentage: Int) {
        companion object {
            val FIT_PAGE = ZoomLevel(-1)
            val FIT_WIDTH = ZoomLevel(-2)
            val FIT_CONTENT = ZoomLevel(-3)
        }

        init {
            require(percentage in 8..6400) { "Percentage must be in range [8,6400]" }
        }
    }
}

private val Boolean.bit: Int
    get() = if (this) 1 else 0
