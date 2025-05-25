package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/**
 * Execute Okular commands.
 *
 * @author Abby Berkers
 */
object OkularViewer : InternalPdfViewer("Okular", "okular") {


    /**
     * Temporary variable to store the pdf file path.
     */
    private var pdfFilePath: String? = null

    /**
     * Execute a forward search, opens the pdf file in okular with the line that corresponds to the cursor roughly in the center.
     * Unfortunately this line does not get highlighted.
     *
     * @param outputPath Full path of the pdf.
     * @param sourceFilePath Full path of the tex file.
     * @param line Line number in the source file to navigate to in the pdf.
     */
    override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        if(outputPath != null) {
            pdfFilePath = outputPath
        }

        if (pdfFilePath == null) {
            Notification("LaTeX", "Could not execute forward search", "Please make sure you have compiled the document first.", NotificationType.ERROR).notify(project)
            return
        }
        // This okular command opens the pdf file using the destination coming from the line in the tex file.
        val command = "okular --noraise --unique '$outputPath#src:$line $sourceFilePath'"
        Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
    }
}