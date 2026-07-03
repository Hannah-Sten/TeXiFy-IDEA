package nl.hannahsten.texifyidea.run.pdfviewer

import nl.hannahsten.texifyidea.TexifyBundle

object NoViewer : PdfViewer {

    override val name: String = "none"
    override val displayName: String = TexifyBundle.message("run.pdfviewer.none.displayName")
    override val isFocusSupported = false
    override val isForwardSearchSupported = false

    override fun toString(): String = displayName

    override fun isAvailable(): Boolean {
        return true // set it as a backup empty viewer
    }
}
