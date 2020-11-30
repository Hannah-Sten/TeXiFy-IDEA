package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.ide.actions.OpenInRightSplitAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

class IntelliJPdfViewer : ExternalPdfViewer {
    override val displayName: String = "IntelliJ PDF Viewer"
    override val name: String = displayName.capitalize().replace(" ", "")

    override fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
        pdfPath ?: return
        val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(pdfPath) ?: return
        val pdfEditor = OpenFileDescriptor(project, file)
        ApplicationManager.getApplication().invokeLater { OpenInRightSplitAction.openInRightSplit(project, file, pdfEditor) }
    }

    override fun toString(): String = displayName
}