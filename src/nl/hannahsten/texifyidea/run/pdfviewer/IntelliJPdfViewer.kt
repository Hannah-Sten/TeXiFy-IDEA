package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

class IntelliJPdfViewer : ExternalPdfViewer {
    override val displayName: String = "IntelliJ PDF Viewer"
    override val name: String = displayName.capitalize().replace(" ", "")

    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        Notification("LaTeX", "Internal viewer", "Test", NotificationType.INFORMATION).notify(project)
    }

    override fun toString(): String = displayName
}