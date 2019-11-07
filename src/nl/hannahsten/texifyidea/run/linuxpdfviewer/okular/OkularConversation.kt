package nl.hannahsten.texifyidea.run.linuxpdfviewer.okular

import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.linuxpdfviewer.ViewerConversation

/**
 * Execute Okular commands.
 *
 * @author Abby Berkers
 */
object OkularConversation : ViewerConversation() {
    private var pdfFilePath: String? = null

    /**
     * Execute a forward search, opens the pdf file in okular with the line that corresponds to the cursor roughly in the center.
     * Unfortunately this line does not get highlighted.
     *
     * @param pdfPath Full path of the pdf.
     * @param sourceFilePath Full path of the tex file.
     * @param line Line number in the source file to navigate to in the pdf.
     */
    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int) {
        if (pdfPath != null) {
            pdfFilePath = pdfPath
        }

        if (pdfFilePath != null) {
            // This okular command opens the pdf file using the destination coming from the line in the tex file.
            val command = "okular --unique '$pdfFilePath#src:$line $sourceFilePath'"
            Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
        }
        else {
            throw TeXception("Could not find the pdf file.")
        }
    }
}