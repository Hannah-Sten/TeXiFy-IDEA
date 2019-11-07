package nl.hannahsten.texifyidea.run.linuxpdfviewer

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.linuxpdfviewer.evince.EvinceConversation
import nl.hannahsten.texifyidea.run.linuxpdfviewer.okular.OkularConversation

/**
 * List of supported PDF viewers on Linux.
 *
 * @param viewerCommand The command to call the viewer from the command line.
 * @param conversation The conversation class needed/used to talk to this viewer.
 */
enum class PdfViewer(private val viewerCommand: String,
                     val conversation: ViewerConversation) {
    EVINCE("evince", EvinceConversation),
    OKULAR("okular", OkularConversation);

    /**
     * Check if the viewer is installed and available from the path.
     */
    fun isAvailable() : Boolean {
        // Only support Evince on Linux, although it can be installed on other systems like Mac.
        if (!SystemInfo.isLinux) {
            return false
        }

        // Find out whether the pdf viewer is installed and in PATH, otherwise we can't use it.
        val output = "which ${this.viewerCommand}".runCommand()
        return output?.contains("/${this.viewerCommand}") ?: false
    }
}