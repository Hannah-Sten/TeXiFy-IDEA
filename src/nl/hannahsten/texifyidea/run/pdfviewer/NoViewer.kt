package nl.hannahsten.texifyidea.run.pdfviewer

object NoViewer : PdfViewer {

    override val name: String = ""
    override val displayName: String = "No PDF viewer"

    override fun toString(): String {
        return displayName
    }

    override fun isAvailable(): Boolean {
        return true // set it as a backup empty viewer
    }
}