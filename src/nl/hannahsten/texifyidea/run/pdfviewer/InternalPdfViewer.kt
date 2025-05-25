package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.util.runCommand

sealed class InternalPdfViewer(
    final override val displayName: String,
    val viewerCommand: String,
) : PdfViewer {

    override val name: String
        get() = viewerCommand

    override fun isAvailable(): Boolean {
        if (SystemInfo.isWindows) {
            val output = "where $viewerCommand".runCommand()
            return output?.contains("\\$viewerCommand") ?: false
        }
        else if (SystemInfo.isLinux) {
            // Find out whether the pdf viewer is installed and in PATH, otherwise we can't use it.
            val output = "which $viewerCommand".runCommand()
            return output?.contains("/$viewerCommand") ?: false
        }
        else if (SystemInfo.isMac) {
            // Check if Skim is installed in applications, otherwise we can't use it.
            // Open -Ra returns an error message if application was not found, else empty string
            val output = "open -Ra $viewerCommand".runCommand()
            return output?.isEmpty() ?: false
        }
        else {
            return false
        }
    }

    override fun isForwardSearchSupported(): Boolean {
        return true
    }


    companion object {


        /**
         * The list of all available internal PDF viewers.
         */
        val allViewers : List<InternalPdfViewer> = listOf(
            SumatraViewer, EvinceViewer, OkularViewer, ZathuraViewer, SkimViewer, NoneViewer
        )

        // These properties may be used often when opening a project or during project use because of settings state initialization, so we cache them.
        val availableSubset: List<InternalPdfViewer> by lazy {
            allViewers.filter { it.isAvailable() }
        }

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