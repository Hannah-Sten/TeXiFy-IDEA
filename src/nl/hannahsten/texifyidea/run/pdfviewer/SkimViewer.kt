package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.TexifyBundle

/**
 * Execute Skim commands.
 *
 * @author Stephan Sundermann
 */
object SkimViewer : SystemPdfViewer("Skim", "skim") {

    @Volatile
    private var pdfFilePath: String? = null

    override val isFocusSupported: Boolean
        get() = true

    override fun openFile(pdfPath: String, project: Project, newWindow: Boolean, focusAllowed: Boolean, forceRefresh: Boolean, raiseOnError: Boolean): Pair<Boolean, String> {
        if (pdfFilePath == null || pdfFilePath != pdfPath) {
            pdfFilePath = pdfPath
        }
        return Pair(true, "")
    }

    /**
     * Execute a forward search, opens the pdf file in Skim with the line that corresponds to the cursor roughly in the center.
     *
     * @param outputPath Full path of the pdf.
     * @param sourceFilePath Full path of the tex file.
     * @param line Line number in the source file to navigate to in the pdf.
     */
    override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean, raiseOnError: Boolean): Pair<Boolean, String> {
        if (outputPath != null) {
            pdfFilePath = outputPath
        }
        if (pdfFilePath == null) {
            return Pair(false, TexifyBundle.message("run.notification.forward.search.failed.compile.first"))
        }
        // This command opens the pdf file using the destination coming from the line in the tex file.
        val backgroundParameter = if (focusAllowed) "" else "-g"
        val command = "/Applications/Skim.app/Contents/SharedSupport/displayline $backgroundParameter -r $line '$pdfFilePath' '$sourceFilePath'"
        Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
        return Pair(true, "")
    }
}
