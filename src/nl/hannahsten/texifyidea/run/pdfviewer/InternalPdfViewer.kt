package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.pdfviewer.evince.EvinceConversation
import nl.hannahsten.texifyidea.run.pdfviewer.okular.OkularConversation
import nl.hannahsten.texifyidea.run.pdfviewer.skim.SkimConversation
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.SumatraConversation
import nl.hannahsten.texifyidea.run.pdfviewer.zathura.ZathuraConversation
import nl.hannahsten.texifyidea.run.sumatra.SumatraAvailabilityChecker
import nl.hannahsten.texifyidea.util.runCommand
import kotlin.reflect.full.createInstance

/**
 * List of supported PDF viewers on Linux.
 *
 * These pdf viewers have their support built-in in TeXiFy.
 * @param executableName The command to call the viewer from the command line.
 * @param conversation The conversation class needed/used to talk to this viewer.
 */
sealed class InternalPdfViewer(
    override val executableName: String,
    override val displayName: String,
    val conversation: ViewerConversation?
) : SupportedPdfViewer {

    override val displayType: String
        get() = "PDF Viewer"

    override fun isAvailable(): Boolean = availability[this] ?: false

    /**
     * Check if the viewer is installed and available from the path.
     */
    fun checkAvailability(): Boolean {
        return if (SystemInfo.isWindows && this.executableName == "sumatra") {
            SumatraAvailabilityChecker.getSumatraAvailability()
        }
        // Only support Evince and Okular on Linux, although they can be installed on other systems like Mac.
        else if (SystemInfo.isLinux) {
            // Find out whether the pdf viewer is installed and in PATH, otherwise we can't use it.
            val output = "which ${this.executableName}".runCommand()
            output?.contains("/${this.executableName}") ?: false
        }
        else if (SystemInfo.isMac) {
            // Check if Skim is installed in applications, otherwise we can't use it.
            // Open -Ra returns an error message if application was not found, else empty string
            val output = "open -Ra ${this.executableName}".runCommand()
            output?.isEmpty() ?: false
        }
        else {
            false
        }
    }

    override fun toString(): String = displayName

    companion object {

        private val availability: Map<InternalPdfViewer, Boolean> by lazy {
            InternalPdfViewer::class.sealedSubclasses.map { it.createInstance() }.associateWith {
                it.checkAvailability()
            }
        }

        fun valueOf(value: String?): InternalPdfViewer? {
            return InternalPdfViewer::class.sealedSubclasses
                .firstOrNull { it.simpleName == value }
                ?.constructors
                ?.firstOrNull()
                ?.call(value)
        }

        fun availableSubset(): List<InternalPdfViewer> = availability.entries.filter { it.value }.map { it.key }

        fun firstAvailable(): InternalPdfViewer? = availableSubset().firstOrNull()
    }
}

class Evince(override val name: String = "Evince") : InternalPdfViewer("evince", "Evince", EvinceConversation)
class Okular(override val name: String = "Okular") : InternalPdfViewer("okular", "Okular", OkularConversation)
class Zathura(override val name: String = "Zathura") : InternalPdfViewer("zathura", "Zathura", ZathuraConversation)
class Skim(override val name: String = "Skim") : InternalPdfViewer("skim", "Skim", SkimConversation)
class Sumatra(override val name: String = "Sumatra") : InternalPdfViewer("sumatra", "Sumatra", SumatraConversation)
