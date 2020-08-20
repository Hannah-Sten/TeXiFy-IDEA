package nl.hannahsten.texifyidea.run.linuxpdfviewer.zathura

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.linuxpdfviewer.ViewerConversation
import nl.hannahsten.texifyidea.run.linuxpdfviewer.okular.OkularConversation

object ZathuraConversation : ViewerConversation() {
    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        if (pdfPath != null) {
            // This okular command opens the pdf file using the destination coming from the line in the tex file.
            val command = """zathura --synctex-forward="$line:1:$sourceFilePath" $pdfPath"""
            Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
        }
        else {
            Notification("OkularConversation", "Could not execute forward search", "Please make sure you have compiled the document first.", NotificationType.ERROR).notify(project)
        }
    }
}