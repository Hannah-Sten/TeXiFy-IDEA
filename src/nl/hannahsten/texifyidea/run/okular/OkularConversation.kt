package nl.hannahsten.texifyidea.run.okular

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.runCommand
import java.io.File

/**
 * Checks if Okular is available.
 */
fun isOkularAvailable() : Boolean {
    // Only support Evince on Linux, although it can be installed on other systems like Mac
    if (!SystemInfo.isLinux) {
        return false
    }

    // Find out whether Okular is installed and in PATH, otherwise we can't use it
    val output = "which okular".runCommand()
    return output?.contains("/okular") ?: false
}

/**
 * Execute Okular commands.
 *
 * @author Abby Berkers
 */
object OkularConversation {
    private var pdfFilePath: String? = null

    /**
     * Execute a forward search, opens the pdf file in okular with the line that corresponds to the cursor roughly in the center.
     * Unfortunately this line does not get highlighted.
     *
     * @param pdfPath Full path of the pdf.
     * @param texFilePath Full path of the tex file.
     * @param line Line number in the source file to navigate to in the pdf.
     */
    fun forwardSearch(pdfPath: String?, texFilePath: String, line: Int) {
        if (pdfPath != null) {
            pdfFilePath = pdfPath
        }

        if (pdfFilePath != null) {
            // This okular command opens the pdf file using the destination coming from the line in the tex file.
            val command = "okular --unique '$pdfFilePath#src:$line $texFilePath'"
            Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
        }
        else {
            throw TeXception("Could not find the pdf file.")
        }
    }
}