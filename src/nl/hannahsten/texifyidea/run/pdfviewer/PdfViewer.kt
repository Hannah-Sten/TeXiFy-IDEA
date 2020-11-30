package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.string.printToString

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
interface PdfViewer {
    val name: String?
    val displayName: String?
}

/**
 * Define behaviour that external pdf viewers should inherit.
 */
interface ExternalPdfViewer : PdfViewer {
    fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean)
}

object ExternalPdfViewers {
    fun getExternalPdfViewers(): List<ExternalPdfViewer> = EP_NAME.extensionList
}