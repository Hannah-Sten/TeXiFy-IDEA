package nl.hannahsten.texifyidea.run.linuxpdfviewer.zathura

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.linuxpdfviewer.ViewerConversation
import nl.hannahsten.texifyidea.run.linuxpdfviewer.okular.OkularConversation

object ZathuraConversation : ViewerConversation() {
    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        if (pdfPath != null) {
            val path = PathManager.getBinPath()
            val name = ApplicationNamesInfo.getInstance().scriptName
            val command = """zathura --synctex-forward="$line:1:$sourceFilePath" --synctex-editor-command="$path/$name.sh --line %{line} $sourceFilePath" $pdfPath"""
            Runtime.getRuntime().exec(arrayOf("bash", "-c", command))
        }
        else {
            Notification("ZathuraConversation", "Could not execute forward search", "Please make sure you have compiled the document first.", NotificationType.ERROR).notify(project)
        }
    }
}