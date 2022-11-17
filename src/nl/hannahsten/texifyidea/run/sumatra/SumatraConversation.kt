package nl.hannahsten.texifyidea.run.sumatra

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.pretty_tools.dde.client.DDEClientConversation
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.linuxpdfviewer.ViewerConversation
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.runCommandWithoutReturn
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import nl.hannahsten.texifyidea.util.runCommand
import java.io.File

/**
 * Indicates whether SumatraPDF is installed and DDE communication is enabled.
 *
 * Is computed once at initialization (for performance), which means that the IDE needs to be restarted when users
 * install SumatraPDF while running TeXiFy.
 */
object SumatraAvailabilityChecker {

    private var isSumatraAvailable: Boolean = false

    private var sumatraWorkingCustomDir: File? = null

    private val isSumatraAvailableInit: Boolean by lazy {
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

    init {
        isSumatraAvailable = isSumatraAvailableInit
    }

    fun getSumatraAvailability(): Boolean {
        return isSumatraAvailable
    }

    fun getSumatraWorkingCustomDir(): File? {
        return sumatraWorkingCustomDir
    }

    /**
     * Checks if Sumatra can be found in a global PATH or in a directory (with sumatraCustomPath)
     * Verifies that sumatraCustomPath is a directory, non-null and non-empty before checking in the directory for Sumatra.
     */
    fun isSumatraPathAvailable(sumatraCustomPath: String? = null, assignNewAvailability: Boolean = true): Pair<Boolean, File?> {
        var workingDir: File? = null
        if (!sumatraCustomPath.isNullOrEmpty() && File(sumatraCustomPath).isDirectory) {
            workingDir = File(sumatraCustomPath)
        }

        val availabilityParams = Pair(runCommandWithExitCode("where", "SumatraPDF", workingDirectory = workingDir).second == 0, workingDir)

        if (assignNewAvailability && !isSumatraAvailableInit) {
            isSumatraAvailable = availabilityParams.first
            if (isSumatraAvailable && workingDir != null) {
                sumatraWorkingCustomDir = workingDir
            }
        }

        return availabilityParams
    }

    private fun isSumatraInstalled(): Boolean {
        // Try some SumatraPDF registry keys
        // For some reason this first one isn't always present anymore, it used to be
        val regQuery1 = runCommand("reg", "query", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\SumatraPDF.exe", "/ve")?.startsWith("ERROR:") == false
        val regQuery2 = runCommand("reg", "query", "HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\SumatraPDF.pdf", "/ve")?.startsWith("ERROR:") == false

        if (regQuery1 || regQuery2) return true

        // Try if Sumatra is in PATH
        return isSumatraPathAvailable(sumatraCustomPath = null, assignNewAvailability = false).first
    }
}

/**
 * Send commands to SumatraPDF.
 *
 * This is available on Windows only.
 *
 * @author Sten Wessel
 * @since b0.4
 */
object SumatraConversation : ViewerConversation() {

    private const val server = "SUMATRA"
    private const val topic = "control"
    private var conversation: DDEClientConversation? = null

    private fun openConversation() {
        if (SumatraAvailabilityChecker.getSumatraAvailability()) {
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
        openConversation()
        try {
            execute("Open(\"$pdfFilePath\", ${newWindow.bit}, ${focus.bit}, ${forceRefresh.bit})")
        }
        catch (e: TeXception) {
            // Added checks when sumatraPath doesn't exist (not a directory), so Windows popup error doesn't appear
            val (_, workingDir) = SumatraAvailabilityChecker.isSumatraPathAvailable(sumatraPath)
            if (SumatraAvailabilityChecker.getSumatraAvailability()) {
                runCommandWithoutReturn("cmd.exe", "/C", "start", "SumatraPDF", "-reuse-instance", pdfFilePath, workingDirectory = workingDir)
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
