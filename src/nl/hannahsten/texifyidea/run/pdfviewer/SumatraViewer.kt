package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.pretty_tools.dde.DDEException
import com.pretty_tools.dde.DDEMLException
import com.pretty_tools.dde.client.DDEClientConversation
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer.sumatraRunnable
import nl.hannahsten.texifyidea.util.runCommand
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.pathString

/**
 * Send commands to SumatraPDF.
 *
 * This is available on Windows only.
 *
 * @author Sten Wessel
 * @since b0.4
 */
object SumatraViewer : SystemPdfViewer("SumatraPDF", "SumatraPDF") {

    private const val SERVER = "SUMATRA"
    private const val TOPIC = "control"


    /*
    SumatraPDF can be used in two ways: command line and DDE.

    https://github.com/sumatrapdfreader/sumatrapdf/blob/master/docs/md/Command-line-arguments.md
     */

    /**
     * A thread-safe DDE conversation to communicate with SumatraPDF.
     */
    private val conversation: DDEClientConversation? by lazy {
        try {
            DDEClientConversation()
        }
        // This happens when the native library DLLs could not be found.
        catch (e: NoClassDefFoundError) {
            null
        }
        catch (e: UnsatisfiedLinkError) {
            null
        }
    }


    /*
 * Indicates whether SumatraPDF is installed and DDE communication is enabled.
 *
 * Is computed once at initialization (for performance), which means that the IDE needs to be restarted when users
 * install SumatraPDF while running TeXiFy.
 */

    /**
     * If we know a valid path containing SumatraPDF.exe, it will be stored here, in case as a last resort you really just want to open a Sumatra, doesn't matter which one.
     *
     */
    @Volatile
    private var sumatraRunnable: Path? = null

    @Volatile
    private var previousPdfPath: String? = null


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
     * Checks the availability of SumatraPDF on the system and sets the [sumatraRunnable] variable
     */
    override fun checkAvailabilityOnSystem(possiblePath: String?): Boolean {
        if (!SystemInfo.isWindows) return false
        if (possiblePath != null && trySumatraRunnablePath(possiblePath)) {
            // If a possible path is provided, we can try to assign it directly
            return true
        }
        // If we already know a valid path, we can skip the rest of the checks
        sumatraRunnable?.let {
            if (trySumatraRunnablePath(it)) return true
        }

        val paths = runCommand("where", "SumatraPDF", workingDirectory = null)
        paths?.split("\n")?.firstOrNull()?.let {
            sumatraRunnable = Path(it).toAbsolutePath()
            return true
        }
        // Try SumatraPDF in the following locations
        listOf(
            "${System.getenv("HOMEDRIVE")}${System.getenv("HOMEPATH")}\\AppData\\Local\\SumatraPDF\\SumatraPDF.exe",
            "C:\\Users\\${System.getenv("USERNAME")}\\AppData\\Local\\SumatraPDF\\SumatraPDF.exe",
            "C:\\Program Files\\SumatraPDF\\SumatraPDF.exe",
        ).map { Path(it) }
            .forEach {
                if (it.exists()) {
                    sumatraRunnable = it.toAbsolutePath()
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
            sumatraRunnable = Path(runnablePath).toAbsolutePath()
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

    fun trySumatraRunnablePath(path: String): Boolean {
        return try {
            trySumatraRunnablePath(Path(path))
        }
        catch (e: InvalidPathException) {
            // If the path is not valid, we just return false
            false
        }
    }

    fun trySumatraRunnablePath(path: Path): Boolean {
        if (!Files.exists(path) || !Files.isExecutable(path)) {
            return false
        }
        if (path.name != "SumatraPDF.exe") return false
        sumatraRunnable = path
        return true
    }


    private fun runSumatraCommand(vararg args: String): Pair<String?, Int> {
        // Run the command in a new process and return the output and exit code
        val sumatraCommand = sumatraRunnable?.pathString ?: return Pair(null, 1)
        val res = runCommandWithExitCode(sumatraCommand, *args)
        return res
    }

    /**
     * Open a file in SumatraPDF, starting it if it is not running yet.
     */
    override fun openFile(pdfPath: String, project: Project, newWindow: Boolean, focus: Boolean, forceRefresh: Boolean) {
        if (!isAvailable()) return
        val quotedPdfPath = "\"$pdfPath\""
        if (conversation != null) {
            try {
                execute("Open($quotedPdfPath, ${newWindow.bit}, ${focus.bit}, ${forceRefresh.bit})")
                return
            }
            catch (e: TeXception) {
            }
        }
        runCommand("cmd.exe", "/C", "start", "SumatraPDF", "-reuse-instance", pdfPath, workingDirectory = sumatraRunnable?.parent?.toFile())
//        runSumatraCommand("-reuse-instance", quotedPdfPath)
    }

    override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        if (!isAvailable()) return
        forwardSearch(outputPath, sourceFilePath, line, focus = focusAllowed)
    }

    /**
     * Execute forward search, highlighting a certain line in SumatraPDF.
     */
    fun forwardSearch(pdfFilePath: String? = null, sourceFilePath: String, line: Int, newWindow: Boolean = false, focus: Boolean = false) {
        if (pdfFilePath != null) {
            previousPdfPath = pdfFilePath
        }
        if (conversation != null) {
            // let SumatraPDF determine the PDF file path if it is not provided
            val pdfPath = if (pdfFilePath != null) "\"$pdfFilePath\", " else ""
            execute("ForwardSearch($pdfPath\"$sourceFilePath\", $line, 0, ${newWindow.bit}, ${focus.bit})")
        }
        else {
            // Use command line to perform forward search, then we'd better have a valid pdfFilePath
            val pdfPath = pdfFilePath ?: previousPdfPath ?: ""
            runSumatraCommand("-forward-search", sourceFilePath, line.toString(), pdfPath)
            return
        }
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
        val conversation = this.conversation ?: throw TeXception(
            "DDE conversation could not be initialized. " +
                    "Please ensure that the native library DLLs are available in the classpath."
        )
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
        val sumatraWorkingDir = sumatraRunnable?.parent?.toFile() ?: return

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
            runCommandWithExitCode(
                "cmd.exe", "/C", "start", "SumatraPDF", "-inverse-search", "\"$path\\$name.exe\" --line %l \"%f\"",
                workingDirectory = sumatraWorkingDir, discardOutput = true
            )
        }
        else {
            runCommandWithExitCode(
                "cmd.exe", "/C", "start", "SumatraPDF", "-inverse-search", "\"$path\\$name.exe\" \"\" --line %l \"%f\"",
                workingDirectory = sumatraWorkingDir, discardOutput = true
            )
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
