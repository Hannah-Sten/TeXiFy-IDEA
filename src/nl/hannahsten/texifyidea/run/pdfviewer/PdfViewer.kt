package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.extensions.ExtensionPointName
import nl.hannahsten.texifyidea.run.executable.Executable
import nl.hannahsten.texifyidea.run.executable.SupportedExecutable

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

        override fun toString(value: PdfViewer) = value.name

        override fun fromString(value: String): PdfViewer =
            ExternalPdfViewers.getExternalPdfViewers().firstOrNull { it.name == value }
                ?: InternalPdfViewer.valueOf(value)
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