package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.util.runCommand

/**
 * Allow other plugins to define their own pdf viewers.
 *
 * We also use this extension point to define the Intellij internal pdf viewer, to make sure that
 * TeXiFy only uses it when the (optional dependency) IntelliJ PDF Viewer plugin is installed.
 */
private val EP_NAME = ExtensionPointName<ExternalPdfViewer>("nl.rubensten.texifyidea.pdfViewer")

/**
 * Interface that defines the general behavior of a PDF viewer.
 */
interface PdfViewer {

    /**
     * The identifier of the viewer.
     */
    val name: String?
    val displayName: String?

    /**
     * Tells whether the PDF viewer is available.
     */
    fun isAvailable(): Boolean

    /**
     * Whether the PDF viewer supports forward search.
     * Default is true for backwards compatibility with the PDF Viewer plugin
     */
    val isForwardSearchSupported: Boolean
        get() = true

    /**
     * Whether the PDF viewer supports customized focus change during forward search, namely, it can determine to take focus or not when performing forward search.
     */
    val isFocusSupported: Boolean
        get() = false

    /**
     * Performs a forward search to navigate from the source file to the corresponding location in the document.
     *
     * @param outputPath The file path to the output document. If null, the viewer may attempt to determine the path automatically.
     * @param sourceFilePath The file path to the source file (e.g., a LaTeX file) that corresponds to the document.
     * @param line The line number in the source file to which the viewer should navigate.
     * @param project The project context in which the forward search is performed.
     * @param focusAllowed Indicates whether the viewer is allowed to take focus when performing the forward search.
     * **If false, the viewer should not take focus anyway, even if it has to abort the operation.** (In order to not disturb the user.)
     */
    fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        // Default implementation does nothing.
    }

    /**
     * Opens a PDF file in the viewer.
     *
     * @param pdfPath The file path to the PDF document that should be opened.
     * @param project The project context associated with the PDF file.
     * @param newWindow Indicates whether the PDF should be opened in a new window.
     * @param focusAllowed Indicates whether the viewer should take focus when opening the file.
     * **If false, the viewer should not take focus anyway, even if it has to abort the operation.** (In order to not disturb the user.)
     * @param forceRefresh Indicates whether the viewer should force a refresh of the file content.
     */
    fun openFile(pdfPath: String, project: Project, newWindow: Boolean = false, focusAllowed: Boolean = true, forceRefresh: Boolean = false) {
        // Default implementation does nothing.
    }

    companion object {

        val internalViewers
            get() = listOf(SumatraViewer, EvinceViewer, OkularViewer, ZathuraViewer, SkimViewer, SystemDefaultViewer, NoViewer)

        val externalViewers
            get() = EP_NAME.extensionList

        /**
         * Gets the list of all PDF viewers, both internal and external.
         */
        val allViewers: List<PdfViewer>
            get() = externalViewers + internalViewers

        /**
         * Gets the list of all available PDF viewers, both internal and external.
         */
        val availableViewers: List<PdfViewer>
            get() = allViewers.filter { it.isAvailable() }

        val firstAvailableViewer: PdfViewer
            get() {
                if (SystemInfo.isLinux) {
                    // Use system default if possible
                    return runCommand("xdg-mime", "query", "default", "application/pdf", timeout = 1)?.let { mime ->
                        availableViewers.firstOrNull { viewer ->
                            val name = viewer.name ?: return@firstOrNull false
                            name.lowercase() in mime.lowercase()
                        }
                    } ?: availableViewers.first()
                }
                else {
                    return allViewers.first { it.isAvailable() }
                }
            }
    }
}

/**
 * Define behaviour that external pdf viewers should inherit.
 *
 * This interface is kept for backwards compatibility.
 */
interface ExternalPdfViewer : PdfViewer