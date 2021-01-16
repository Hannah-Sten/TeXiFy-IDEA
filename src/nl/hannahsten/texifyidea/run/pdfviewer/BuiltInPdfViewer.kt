package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.ide.actions.OpenInRightSplitAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * IntelliJ built-in PDF viewer provided by the [IntelliJ PDF Viewer plugin](https://github.com/FirstTimeInForever/intellij-pdf-viewer).
 *
 * External in the sense that it is defined as an [ExternalPdfViewer] because we use an extension point to define the
 * behaviour of this viewer. We only provide this implementation of the extension point when the IntelliJ PDF Viewer plugin
 * is installed, hence this viewer is not available when that plugin is not installed.
 *
 * See the [Intellij Forum](https://intellij-support.jetbrains.com/hc/en-us/community/posts/360009392960/comments/360001899860).
 */
class BuiltInPdfViewer : ExternalPdfViewer {

    override val displayName: String = "Built-in PDF Viewer"

    /**
     * The built-in pdf viewer is always available when the pdf viewer plugin is installed.
     */
    override fun isAvailable(): Boolean = true

    override val name: String = displayName.toUpperCase().replace(" ", "-")

    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        pdfPath ?: return
        val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(pdfPath) ?: return
        val pdfEditor = OpenFileDescriptor(project, file)
        ApplicationManager.getApplication().invokeLater { OpenInRightSplitAction.openInRightSplit(project, file, pdfEditor) }
    }

    override fun toString(): String = displayName
}