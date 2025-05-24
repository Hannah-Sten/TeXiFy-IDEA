package nl.hannahsten.texifyidea.run.sumatra

import com.intellij.openapi.project.Project
import com.pretty_tools.dde.DDEException
import com.pretty_tools.dde.DDEMLException
import com.pretty_tools.dde.client.DDEClientConversation
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.linuxpdfviewer.ViewerConversation
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

/**
 * Send commands to SumatraPDF.
 *
 * This is available on Windows only.
 *
 * @author Sten Wessel
 * @since b0.4
 */
object SumatraConversation : ViewerConversation() {

    private const val SERVER = "SUMATRA"
    private const val TOPIC = "control"
    private var conversation: DDEClientConversation? = null

    private fun openConversation() {
        if (SumatraAvailabilityChecker.isSumatraAvailable && conversation == null) {
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
    fun openFile(pdfFilePath: String, newWindow: Boolean = false, focus: Boolean = false, forceRefresh: Boolean = false) {
        try {
            execute("Open(\"$pdfFilePath\", ${newWindow.bit}, ${focus.bit}, ${forceRefresh.bit})")
        }
        catch (e: TeXception) {
            // Make sure Windows popup error doesn't appear and we will still open Sumatra
            if (SumatraAvailabilityChecker.isSumatraAvailable) {
                runCommandWithExitCode("cmd.exe", "/C", "start", "SumatraPDF", "-reuse-instance", pdfFilePath, workingDirectory = SumatraAvailabilityChecker.sumatraDirectory, discardOutput = true)
            }
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
        openConversation()
        try {
            conversation!!.connect(SERVER, TOPIC)
            conversation!!.execute(commands.joinToString(separator = "") { "[$it]" })
        }
        catch (e: DDEException) {
            throw convertErrorAndMessage(e)
        }
        finally {
            conversation?.disconnect()
        }
    }

    private fun convertErrorAndMessage(e: DDEException): TeXception {
        if (e !is DDEMLException) {
            return TeXception("Failed to execute command in SumatraPDF.", e)
        }
        val message =
            when (e.errorCode) {
                // This occurs when an output file is not found
                DDEMLException.DMLERR_NOTPROCESSED -> "Cannot find the output file."
                else -> e.message ?: "Unknown error."
            }
        return TeXception(message, e)
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
        CONTINUOUS_BOOK_VIEW("continuous book view")
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
