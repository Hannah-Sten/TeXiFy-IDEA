package nl.hannahsten.texifyidea.run.sumatra

import com.intellij.openapi.util.SystemInfo
import com.pretty_tools.dde.client.DDEClientConversation
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.runCommand
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import java.io.File

/**
 * Indicates whether SumatraPDF is installed and DDE communication is enabled.
 *
 * Is computed once at initialization (for performance), which means that the IDE needs to be restarted when users
 * install SumatraPDF while running TeXiFy.
 */
object SumatraAvailabilityChecker {

    var isSumatraAvailable: Boolean = isSumatraInstalledAndAvailable()
        private set

    /** If we know a valid path containing SumatraPDF.exe, it will be stored here, in case as a last resort you really just want to open a Sumatra, doesn't matter which one. */
    var sumatraDirectory: File? = null

    private fun isSumatraInstalledAndAvailable(): Boolean {
        if (!SystemInfo.isWindows || !isSumatraInstalled()) return false

        // Try if native bindings are available
        // Note: this will not throw any exception when Sumatra is not installed, that will only happen when we really try to connect()
        try {
            DDEClientConversation()
        }
        catch (e: UnsatisfiedLinkError) {
            Log.info("Native library DLLs could not be found.")
            return false
        }
        catch (e: NoClassDefFoundError) {
            Log.info("Native library DLLs could not be found.")
            return false
        }

        return true
    }

    /**
     * Checks if Sumatra can be found in a global PATH or in a directory (with sumatraCustomPath)
     * Verifies that sumatraCustomPath is a directory, non-null and non-empty before checking in the directory for Sumatra.
     * Updates sumatraWorkingCustomDir and isSumatraAvailable if assignNewAvailability is true
     * Returns a pair, first tells if Sumatra is accessible in PATH or customPath, seconds tells if Sumatra is
     * accessible in customPath
     */
    fun isSumatraPathAvailable(
        sumatraCustomPath: String? = null,
        assignNewAvailability: Boolean = true
    ): Pair<Boolean, Boolean> {
        var workingDir: File? = null
        if (!sumatraCustomPath.isNullOrEmpty() && File(sumatraCustomPath).isDirectory) {
            workingDir = File(sumatraCustomPath)
        }

        var isCustomPathValid = false // if Sumatra is accessible in customPath
        val whereSumatraResult = runCommandWithExitCode("where", "SumatraPDF", workingDirectory = workingDir)
        val isSumatraInAllPath = (whereSumatraResult.second == 0) // if Sumatra is accessible in PATH or customPath

        if (workingDir != null && whereSumatraResult.first?.contains(sumatraCustomPath.toString()) == true && isSumatraInAllPath) {
            isCustomPathValid = true
        }

        if (assignNewAvailability) {
            if (!isSumatraAvailable) {
                isSumatraAvailable = isSumatraInAllPath
            }
            if (workingDir != null) {
                sumatraDirectory = workingDir
            }
        }

        return Pair(isSumatraInAllPath, isCustomPathValid)
    }

    /**
     * Checks at initialization if the Sumatra registry keys are registered or if Sumatra is in PATH.
     * returns true if the Sumatra registry keys are registered or if Sumatra is in PATH.
     */
    private fun isSumatraInstalled(): Boolean {
        // Try if Sumatra is in PATH
        val guessedPaths = listOf(
            null,
            "${System.getenv("HOMEDRIVE")}${System.getenv("HOMEPATH")}AppData\\Local\\SumatraPDF",
            "C:\\Users\\${System.getenv("USERNAME")}\\AppData\\Local\\SumatraPDF",
        )
        for (path in guessedPaths) {
            if (isSumatraPathAvailable(sumatraCustomPath = path, assignNewAvailability = true).first) {
                return true
            }
        }

        // Try some SumatraPDF registry keys
        // For some reason this first one isn't always present anymore, it used to be
        val paths = listOf(
            "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\SumatraPDF.exe",
            "HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\SumatraPDF.pdf",
            "HKEY_CURRENT_USER\\SOFTWARE\\Classes\\SumatraPDF.pdf",
        )
        for (path in paths) {
            if (runCommand("reg", "query", path, "/ve")?.startsWith("ERROR:") == false) {
                // To improve this, we could also update the known installation directory so that we can open Sumatra automatically
                return true
            }
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
}