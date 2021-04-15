package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.util.selectedRunConfig

open class ForwardSearchAction(var viewer: PdfViewer? = null) : EditorAction(
    name = "_Forward Search",
    icon = TexifyIcons.RIGHT
) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (viewer == null) return

        if (!viewer!!.isAvailable()) {
            return
        }

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1

        when (viewer) {
            is ExternalPdfViewer -> (viewer as ExternalPdfViewer).forwardSearch(null, file.path, line, project, focusAllowed = true)
            is InternalPdfViewer -> (viewer as InternalPdfViewer).conversation!!.forwardSearch(null, file.path, line, project, focusAllowed = true)
            else -> return
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project?.selectedRunConfig()?.pdfViewer == viewer
    }
}