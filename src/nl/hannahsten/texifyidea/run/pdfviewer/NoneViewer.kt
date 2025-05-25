package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.project.Project

object NoneViewer : InternalPdfViewer("No PDF viewer", "") {

    override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        // Do nothing.
    }

    override fun isAvailable(): Boolean {
        return false
    }

    override fun isForwardSearchSupported(): Boolean {
        return false
    }
}