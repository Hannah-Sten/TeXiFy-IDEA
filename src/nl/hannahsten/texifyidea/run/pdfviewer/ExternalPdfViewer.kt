package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.executable.SupportedExecutable

/**
 * Define behaviour that external (that means, registered from outside TeXiFy) pdf viewers should inherit.
 */
interface ExternalPdfViewer : PdfViewer, SupportedExecutable {

    override val displayType: String
        get() = "PDF Viewer"

    override val displayName: String
        get() = name

    override val executableName: String
        get() = name

    /**
     * Open the pdf in the pdf file if it is not open yet, and forward search to it.
     */
    fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean)
}