package nl.hannahsten.texifyidea.run.pdfviewer

object NoViewer : PdfViewer {

    override val name: String = "none"
    override val displayName: String = "No PDF viewer"
    override val isFocusSupported = false
    override val isForwardSearchSupported = false

    override fun toString(): String = displayName

    override fun isAvailable(): Boolean {
        return true // set it as a backup empty viewer
    }
}