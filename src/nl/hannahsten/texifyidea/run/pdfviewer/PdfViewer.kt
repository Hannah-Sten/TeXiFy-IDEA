package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

/**
 * Allow other plugins to define their own pdf viewers.
 *
 * We also use this extension point to define the Intellij internal pdf viewer, to make sure that
 * TeXiFy only uses it when the (optional dependency) IntelliJ PDF Viewer plugin is installed.
 */
private val EP_NAME = ExtensionPointName<ExternalPdfViewer>("nl.rubensten.texifyidea.pdfViewer")

/**
 * Interface that defines a pdf viewer so we can use both [nl.hannahsten.texifyidea.run.linuxpdfviewer.InternalPdfViewer]s and [ExternalPdfViewer]s interchangeably.
 */
interface PdfViewer {

    val name: String?
    val displayName: String?

    fun isAvailable(): Boolean

    /**
     * Performs a forward search to navigate from the source file to the corresponding location in the PDF document.
     *
     * @param pdfPath The file path to the PDF document. If null, the viewer may attempt to determine the path automatically.
     * @param sourceFilePath The file path to the source file (e.g., a LaTeX file) that corresponds to the PDF document.
     * @param line The line number in the source file to which the PDF viewer should navigate.
     * @param project The project context in which the forward search is performed.
     * @param focusAllowed Indicates whether the PDF viewer is allowed to take focus when performing the forward search. If false, the viewer may remain in the background.
     */
    fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean)

    /**
     * Opens a PDF file in the viewer.
     *
     * @param pdfPath The file path to the PDF document that should be opened.
     * @param project The project context associated with the PDF file.
     * @param newWindow Indicates whether the PDF should be opened in a new window. Defaults to false.
     * @param focus Indicates whether the viewer should take focus when opening the file. Defaults to false.
     * @param forceRefresh Indicates whether the viewer should force a refresh of the file content. Defaults to false.
     */
    fun openFile(pdfPath: String, project: Project, newWindow: Boolean = false, focus: Boolean = false, forceRefresh: Boolean = false) {
        // Default implementation does nothing.
    }
}

/**
 * Define behaviour that external pdf viewers should inherit.
 */
interface ExternalPdfViewer : PdfViewer

/**
 * Define functions that handle all external pdf viewers one by one.
 */
object ExternalPdfViewers {

    fun getExternalPdfViewers(): List<ExternalPdfViewer> = EP_NAME.extensionList
}