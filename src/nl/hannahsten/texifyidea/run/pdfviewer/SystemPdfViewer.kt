package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.util.runCommand

abstract class SystemPdfViewer(
    final override val displayName: String,
    val viewerCommand: String,
) : PdfViewer {

    override val name: String
        get() = viewerCommand

    override fun toString(): String = displayName

    /**
     * Check if the PDF viewer is available on the system, the result of this function is cached.
     */
    protected open fun checkAvailabilityOnSystem(possiblePath: String? = null): Boolean {
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

    /**
     * Cached availability of the PDF viewer, using [Volatile] to ensure visibility and atomicity
     */
    @Volatile
    private var availability: Boolean? = null

    /**
     * Refresh the availability of the PDF viewer.
     * For example, when the user installs or uninstalls the viewer, this method can be called.
     */
    @Suppress("unused")
    fun refreshAvailability(possiblePath: String? = null) {
        availability = checkAvailabilityOnSystem(possiblePath)
    }

    override fun isAvailable(): Boolean = availability ?: checkAvailabilityOnSystem().also {
        availability = it
    }

    override val isForwardSearchSupported: Boolean
        get() = true
}