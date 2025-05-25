package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.pretty_tools.dde.DDEException
import com.pretty_tools.dde.DDEMLException
import com.pretty_tools.dde.client.DDEClientConversation
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.util.runCommand
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import java.io.File

/**
 * Send commands to SumatraPDF.
 *
 * This is available on Windows only.
 *
 * @author Sten Wessel
 * @since b0.4
 */
object SumatraViewer : InternalPdfViewer("SumatraPDF", "SumatraPDF") {

    private const val SERVER = "SUMATRA"
    private const val TOPIC = "control"


    /*
    SumatraPDF can be used in two ways: command line and DDE.

    - SumatraPDF can be found in path
    - DDEClientConversation can be created
     */

    private var conversation: DDEClientConversation? = null


    /*
 * Indicates whether SumatraPDF is installed and DDE communication is enabled.
 *
 * Is computed once at initialization (for performance), which means that the IDE needs to be restarted when users
 * install SumatraPDF while running TeXiFy.
 */

    /** If we know a valid path containing SumatraPDF.exe, it will be stored here, in case as a last resort you really just want to open a Sumatra, doesn't matter which one. */

    private var sumatraRunnable: File? = null
        @Synchronized get
        @Synchronized set
    private var scanned = false

//    init {
//        scanSumatra()
//    }

    override fun isAvailable(): Boolean {
        if (!SystemInfo.isWindows) return false
        if (sumatraRunnable != null) return true

        synchronized(this) {
            if(!scanned) {
                scanned = true
                scanSumatra()
            }
        }
        return sumatraRunnable != null
    }

    @Synchronized
    private fun initConversation(): DDEClientConversation {
        this.conversation?.let { return it }
        try {
            val conversation = DDEClientConversation()
            this.conversation = conversation
            return conversation
        }
        catch (e: NoClassDefFoundError) {
            throw TeXception("Native library DLLs could not be found.", e)
        }
    }

    private fun getWherePath(res: Pair<String?, Int>): String? {
        val (paths, exitCode) = res
        if (exitCode != 0 || paths == null) {
            return null
        }
        return paths.split("\n").firstOrNull()
    }

    private fun parseRegSumatraPath(regOutput: String): String? {
        /*
        Example output of the command:

        HKEY_CURRENT_USER\SOFTWARE\Classes\SumatraPDF.pdf\shell\open\command
        (Default)    REG_SZ    "C:\Users\username\AppData\Local\SumatraPDF\SumatraPDF.exe" "%1"

         */
        val lines = regOutput.lines()

        val valueLine = lines.find { it.contains("REG_SZ") } ?: return null

        val regex = """REG_SZ\s+(".*?")\s*("%1")?$""".toRegex()
        val match = regex.find(valueLine) ?: return null

        return match.groups[1]?.value?.trim('"')
    }

    /**
     * Checks at initialization if the Sumatra registry keys are registered or if Sumatra is in PATH.
     * returns true if the Sumatra registry keys are registered or if Sumatra is in PATH.
     */
    fun scanSumatra(): Boolean {
        val paths = runCommand("where", "SumatraPDF", workingDirectory = null)
        paths?.split("\n")?.firstOrNull()?.let {
            sumatraRunnable = File(it)
            return true
        }
        // Try SumatraPDF in the following locations
        listOf(
            "${System.getenv("HOMEDRIVE")}${System.getenv("HOMEPATH")}\\AppData\\Local\\SumatraPDF\\SumatraPDF.exe",
            "C:\\Users\\${System.getenv("USERNAME")}\\AppData\\Local\\SumatraPDF\\SumatraPDF.exe",
            "C:\\Program Files\\SumatraPDF\\SumatraPDF.exe",
        ).map { File(it) }
            .forEach {
                if (it.exists()) {
                    sumatraRunnable = it
                    return true
                }
            }

        // Try some SumatraPDF registry keys
        // For some reason this first one isn't always present anymore, it used to be
        val regEntries = listOf(
            "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\SumatraPDF.exe",
            "HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\SumatraPDF.pdf",
            "HKEY_CURRENT_USER\\SOFTWARE\\Classes\\SumatraPDF.pdf",
        )
        for (reg in regEntries) {
            val commandPath = "$reg\\shell\\open\\command"

            val res = runCommand("reg", "query", commandPath, "/ve") ?: continue
            val runnablePath = parseRegSumatraPath(res) ?: continue
            sumatraRunnable = File(runnablePath)
            return true
        }

        // https://github.com/sumatrapdfreader/sumatrapdf/discussions/2855#discussioncomment-3336646

        // We could also look at the values of the following reg keys to find the install path:
        // [HKEY_CURRENT_USER\Software\Classes\SumatraPDF.pdf\shell\open]
        // "Icon"="C:\\Users\\K\\AppData\\Local\\SumatraPDF\\SumatraPDF.exe"
        //
        // [HKEY_CURRENT_USER\Software\Classes\SumatraPDF.pdf\shell\open\command]
        // @="\"C:\\Users\\K\\AppData\\Local\\SumatraPDF\\SumatraPDF.exe\" \"%1\""

        return false
    }

    @Synchronized
    fun assignSumatraRunnable(path: String): Boolean {
        if (path.isEmpty()) return false
        val file = File(path)
        if (!file.exists()) return false
        if (!file.canExecute()) return false
        if (!file.isFile) return false
        if (file.name != "SumatraPDF.exe") return false
        sumatraRunnable = file
        return true
    }


    /**
     * Open a file in SumatraPDF, starting it if it is not running yet.
     */
    override fun openFile(pdfPath: String, project: Project, newWindow: Boolean, focus: Boolean, forceRefresh: Boolean) {
        try {
            execute("Open(\"$pdfPath\", ${newWindow.bit}, ${focus.bit}, ${forceRefresh.bit})")
        }
        catch (e: TeXception) {
            // fallback to command line if DDE fails
            runCommandWithExitCode("cmd.exe", "/C", "start", "SumatraPDF", "-reuse-instance", pdfPath, workingDirectory = sumatraRunnable?.parentFile, discardOutput = true)
        }
    }

    override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        forwardSearch(outputPath, sourceFilePath, line, focus = focusAllowed)
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
        val conversation = initConversation()
        try {
            conversation.connect(SERVER, TOPIC)
            conversation.execute(commands.joinToString(separator = "") { "[$it]" })
        }
        catch (e: DDEException) {
            throw convertErrorAndMessage(e)
        }
        finally {
            conversation.disconnect()
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


    fun configureInverseSearch() {
        val sumatraWorkingDir = sumatraRunnable?.parentFile ?: return

        // First kill Sumatra to avoid having two instances open of which only one has the correct setting
        Runtime.getRuntime().exec(arrayOf("taskkill", "/IM", "SumatraPDF.exe"))
        val path = PathManager.getBinPath()
        var name = ApplicationNamesInfo.getInstance().scriptName

        // If we can find a 64-bits Java, then we can start (the equivalent of) idea64.exe since that will use the 64-bits Java
        // see issue 104 and https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/809
        // If we find a 32-bits Java or nothing at all, we will keep (the equivalent of) idea.exe which is the default
        if (System.getProperty("sun.arch.data.model") == "64") {
            // We will assume that since the user is using a 64-bit IDEA that name64 exists, this is at least true for idea64.exe and pycharm64.exe on Windows
            name += "64"
            // We also remove an extra "" because it opens an empty IDEA instance when present
            runCommandWithExitCode("cmd.exe", "/C", "start", "SumatraPDF", "-inverse-search", "\"$path\\$name.exe\" --line %l \"%f\"", workingDirectory = sumatraWorkingDir, discardOutput = true)
        }
        else {
            runCommandWithExitCode("cmd.exe", "/C", "start", "SumatraPDF", "-inverse-search", "\"$path\\$name.exe\" \"\" --line %l \"%f\"", workingDirectory = sumatraWorkingDir, discardOutput = true)
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
