package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.util.runCommand

/**
 * Open the system default PDF viewer. Does not check whether it is a supported viewer, so no forward search.
 *
 * @author Thomas Schouten
 */
object SystemDefaultViewer : PdfViewer {

    override val name: String = ""
    override val displayName: String = "System default"

    override fun toString(): String {
        return displayName
    }

    override fun isAvailable(): Boolean {
        return !SystemInfo.isWindows
    }

    override fun openFile(pdfPath: String, project: Project, newWindow: Boolean, focusAllowed: Boolean, forceRefresh: Boolean) {
        if (SystemInfo.isMac) {
            // Open default system viewer, source: https://ss64.com/osx/open.html
            runCommand("open", pdfPath)
        }
        else if (SystemInfo.isLinux) {
            // Open default system viewer using xdg-open, since this is available in almost all desktop environments
            runCommand("xdg-open", pdfPath)
        }
    }
}