package nl.hannahsten.texifyidea.run.pdfviewer

internal object CustomPdfViewer : PdfViewer {

    override val name: String = "custom"
    override val displayName: String = "Custom viewer"
    override val isFocusSupported: Boolean = false
    override val isForwardSearchSupported: Boolean = false

    override fun isAvailable(): Boolean = true

    override fun toString(): String = displayName
}
