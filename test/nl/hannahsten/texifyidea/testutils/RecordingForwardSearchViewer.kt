package nl.hannahsten.texifyidea.testutils

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.pdfviewer.NoViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer

class RecordingForwardSearchViewer : PdfViewer {

    override val name: String = NoViewer.name
    override val displayName: String = NoViewer.displayName
    override val isForwardSearchSupported: Boolean = true

    val forwardSearchCalls = mutableListOf<ForwardSearchCall>()

    override fun isAvailable(): Boolean = true

    override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        forwardSearchCalls += ForwardSearchCall(outputPath, sourceFilePath, line, project, focusAllowed)
    }

    data class ForwardSearchCall(
        val outputPath: String?,
        val sourceFilePath: String,
        val line: Int,
        val project: Project,
        val focusAllowed: Boolean,
    )
}
