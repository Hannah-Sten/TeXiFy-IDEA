package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.TexifyBundle

/**
 * Execute Okular commands.
 *
 * @author Abby Berkers
 */
object OkularViewer : SystemPdfViewer("Okular", "okular") {

    /**
     * Temporary variable to store the pdf file path.
     */
    private var pdfFilePath: String? = null

    override val isFocusSupported: Boolean
        get() = false

    /**
     * Execute a forward search, opens the pdf file in okular with the line that corresponds to the cursor roughly in the center.
     * Unfortunately this line does not get highlighted.
     *
     * @param outputPath Full path of the pdf.
     * @param sourceFilePath Full path of the tex file.
     * @param line Line number in the source file to navigate to in the pdf.
     */
    override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean, raiseOnError: Boolean): Pair<Boolean, String> {
        if(outputPath != null) {
            pdfFilePath = outputPath
        }

        if (pdfFilePath == null) {
            return Pair(false, TexifyBundle.message("run.notification.forward.search.failed.compile.first"))
        }
        // This okular command opens the pdf file using the destination coming from the line in the tex file.
        val command = "okular --noraise --unique '$pdfFilePath#src:$line $sourceFilePath'"
        Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
        return Pair(true, "")
    }
}
