package nl.hannahsten.texifyidea.run.linuxpdfviewer.skim

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.linuxpdfviewer.ViewerConversation

/**
 * Execute Skim commands.
 *
 * @author Stephan Sundermann
 */
object SkimConversation : ViewerConversation() {

    private var pdfFilePath: String? = null

    /**
     * Execute a forward search, opens the pdf file in Skim with the line that corresponds to the cursor roughly in the center.
     *
     * @param pdfPath Full path of the pdf.
     * @param sourceFilePath Full path of the tex file.
     * @param line Line number in the source file to navigate to in the pdf.
     */
    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        val backgroundParameter = if (focusAllowed) "" else "-g"

        if (pdfPath != null) {
            pdfFilePath = pdfPath
        }

        if (pdfFilePath != null) {
            // This command opens the pdf file using the destination coming from the line in the tex file.
            val command = "/Applications/Skim.app/Contents/SharedSupport/displayline $backgroundParameter -r $line '$pdfFilePath' '$sourceFilePath'"
            Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
        }
        else {
            Notification("LaTeX", "Could not execute forward search", "Please make sure you have compiled the document first.", NotificationType.ERROR).notify(project)
        }
    }
}
