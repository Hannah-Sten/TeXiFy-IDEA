package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.extensions.ExtensionPointName
import nl.hannahsten.texifyidea.run.executable.Executable
import java.io.File

/**
 * Allow other plugins to define their own pdf viewers.
 *
 * We also use this extension point to define the Intellij internal pdf viewer, to make sure that
 * TeXiFy only uses it when the (optional dependency) IntelliJ PDF Viewer plugin is installed.
 */
private val EP_NAME = ExtensionPointName<ExternalPdfViewer>("nl.rubensten.texifyidea.pdfViewer")

/**
 * Interface that defines a pdf viewer so we can use both [InternalPdfViewer]s and [ExternalPdfViewer]s interchangeably.
 */
interface PdfViewer : Executable {

    val name: String
    override val displayType: String
        get() = "PDF Viewer"

    fun isAvailable(): Boolean

    class Converter : com.intellij.util.xmlb.Converter<PdfViewer>() {

        override fun toString(value: PdfViewer) = if (value is CustomPdfViewer) value.executablePath else value.name

        override fun fromString(value: String): PdfViewer =
            ExternalPdfViewers.getExternalPdfViewers().firstOrNull { it.name == value }
                ?: InternalPdfViewer.valueOf(value)
                ?: if (File(value).exists()) CustomPdfViewer(value) else null
                ?: availablePdfViewers().firstOrNull()
                ?: Evince()
    }
}

/**
 * Define functions that handle all external pdf viewers one by one.
 */
object ExternalPdfViewers {

    fun getExternalPdfViewers(): List<ExternalPdfViewer> = EP_NAME.extensionList
}

fun availablePdfViewers() = ExternalPdfViewers.getExternalPdfViewers() + InternalPdfViewer.availableSubset()