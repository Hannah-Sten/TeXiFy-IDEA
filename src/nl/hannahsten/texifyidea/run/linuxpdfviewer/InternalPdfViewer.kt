package nl.hannahsten.texifyidea.run.linuxpdfviewer

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.linuxpdfviewer.evince.EvinceConversation
import nl.hannahsten.texifyidea.run.linuxpdfviewer.okular.OkularConversation
import nl.hannahsten.texifyidea.run.linuxpdfviewer.skim.SkimConversation
import nl.hannahsten.texifyidea.run.linuxpdfviewer.zathura.ZathuraConversation
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.sumatra.SumatraAvailabilityChecker
import nl.hannahsten.texifyidea.run.sumatra.SumatraConversation
import nl.hannahsten.texifyidea.util.runCommand

/**
 * List of supported PDF viewers on Linux.
 *
 * These pdf viewers have their support built-in in TeXiFy.
 * @param viewerCommand The command to call the viewer from the command line.
 * @param conversation The conversation class needed/used to talk to this viewer.
 */
enum class InternalPdfViewer(
    override val displayName: String,
    private val viewerCommand: String,
    private val conversation: ViewerConversation?
) : PdfViewer {

    EVINCE("evince", "Evince", EvinceConversation),
    OKULAR("okular", "Okular", OkularConversation),
    ZATHURA("zathura", "Zathura", ZathuraConversation),
    SKIM("skim", "Skim", SkimConversation),
    SUMATRA("sumatra", "Sumatra", SumatraConversation),
    NONE("", "No PDF viewer", null);

    /**
     * Check if the viewer is installed and available from the path.
     */
    override fun isAvailable(): Boolean {
        // Using no PDF viewer should always be an option.
        return if (this == NONE) {
            true
        }
        else if (SystemInfo.isWindows && this == SUMATRA) {
            SumatraAvailabilityChecker.isSumatraAvailable
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

    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        conversation?.forwardSearch(pdfPath, sourceFilePath, line, project, focusAllowed)
    }

    companion object {

        // These properties may be used often when opening a project or during project use because of settings state initialization, so we cache them.
        val availableSubset: List<InternalPdfViewer> by lazy { entries.filter { it.isAvailable() } }

        val firstAvailable: InternalPdfViewer by lazy {
            // Use system default if possible
            if (SystemInfo.isLinux) {
                // e.g. okularApplication_pdf.desktop or org.gnome.Evince.desktop
                runCommand("xdg-mime", "query", "default", "application/pdf", timeout = 1)?.let {
                    availableSubset.firstOrNull { viewer -> viewer.name.lowercase() in it.lowercase() }
                } ?: availableSubset.first()
            }
            else {
                availableSubset.first()
            }
        }
    }
}
