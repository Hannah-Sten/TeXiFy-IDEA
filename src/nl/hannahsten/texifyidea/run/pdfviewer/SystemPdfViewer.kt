package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.util.runCommand
import java.io.File

abstract class SystemPdfViewer(
    final override val displayName: String,
    val viewerCommand: String,
) : PdfViewer {

    override val name: String
        get() = viewerCommand

    override fun toString(): String {
        return displayName
    }

    open fun refreshAvailabilityOnSystem() : Boolean {
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
    var availability: Boolean? = null

    override fun isAvailable(): Boolean {
        synchronized(this) {
            if (availability == null) {
                availability = refreshAvailabilityOnSystem()
            }
            return availability ?: false
        }
    }

    override fun isForwardSearchSupported(): Boolean {
        return true
    }

    companion object {


    }
}