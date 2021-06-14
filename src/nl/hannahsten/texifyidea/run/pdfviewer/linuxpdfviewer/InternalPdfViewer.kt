package nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.evince.EvinceConversation
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.okular.OkularConversation
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.skim.SkimConversation
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.zathura.ZathuraConversation
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.isSumatraAvailable
import nl.hannahsten.texifyidea.util.runCommand

/**
 * List of supported PDF viewers on Linux.
 *
 * These pdf viewers have their support built-in in TeXiFy.
 * @param viewerCommand The command to call the viewer from the command line.
 * @param conversation The conversation class needed/used to talk to this viewer.
 */
enum class InternalPdfViewer(
    private val viewerCommand: String,
    override val displayName: String,
    val conversation: ViewerConversation?
) : PdfViewer {

    EVINCE("evince", "Evince", EvinceConversation),
    OKULAR("okular", "Okular", OkularConversation),
    ZATHURA("zathura", "Zathura", ZathuraConversation),
    SKIM("skim", "Skim", SkimConversation),
    SUMATRA("sumatra", "Sumatra", null); // Dummy options to support Windows

    override fun isAvailable(): Boolean = availability[this] ?: false

    /**
     * Check if the viewer is installed and available from the path.
     */
    fun checkAvailability(): Boolean {
        return if (SystemInfo.isWindows && this == SUMATRA) {
            isSumatraAvailable
        }
        // Only support Evince and Okular on Linux, although they can be installed on other systems like Mac.
        else if (SystemInfo.isLinux) {
            // Find out whether the pdf viewer is installed and in PATH, otherwise we can't use it.
            val output = "which ${this.viewerCommand}".runCommand()
            output?.contains("/${this.viewerCommand}") ?: false
        }
        else if (SystemInfo.isMac) {
            // Check if Skim is installed in applications, otherwise we can't use it.
            // Open -Ra returns an error message if application was not found, else empty string
            val output = "open -Ra ${this.viewerCommand}".runCommand()
            output?.isEmpty() ?: false
        }
        else {
            false
        }
    }

    override fun toString(): String = displayName

    companion object {

        private val availability: Map<InternalPdfViewer, Boolean> by lazy {
            values().associateWith {
                it.checkAvailability()
            }
        }

        fun availableSubset(): List<InternalPdfViewer> = values().filter { it.isAvailable() }

        fun firstAvailable(): InternalPdfViewer = availableSubset().first()
    }
}
