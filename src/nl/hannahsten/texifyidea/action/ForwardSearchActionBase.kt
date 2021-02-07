package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.util.selectedRunConfig

open class ForwardSearchActionBase(val viewer: PdfViewer, val id: String = "texify.${viewer.name}") : EditorAction(
    name = "${viewer.displayName} _Forward Search",
    icon = TexifyIcons.RIGHT
) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (!viewer.isAvailable()) {
            return
        }

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1

        when (viewer) {
            is ExternalPdfViewer -> viewer.forwardSearch(null, file.path, line, project, focusAllowed = true)
            is InternalPdfViewer -> viewer.conversation!!.forwardSearch(null, file.path, line, project, focusAllowed = true)
            else -> return
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project?.selectedRunConfig()?.pdfViewer == viewer
    }
}