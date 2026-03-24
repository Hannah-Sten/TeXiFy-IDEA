package nl.hannahsten.texifyidea.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.run.pdfviewer.ForwardSearchSupport
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer

open class ForwardSearchAction(var viewer: PdfViewer? = null) : EditorAction(
    name = "_Forward Search"
) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (file.fileType !is LatexFileType) return
        val viewer = ForwardSearchSupport.resolveViewer(project, file, this.viewer) ?: return
        if (!viewer.isAvailable() || !viewer.isForwardSearchSupported) return

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1
        val outputPath = ForwardSearchSupport.resolveOutputPath(project, file)
        try {
            viewer.forwardSearch(outputPath, file.path, line, project, focusAllowed = true)
            this.viewer = viewer
        }
        catch (e: TeXception) {
            // Show a notification if the forward search fails, but only catch TeXception and let other unexpected exceptions bubble up.
            Notification(
                "LaTeX", "Forward search error", "${e.message}",
                NotificationType.WARNING
            ).notify(project)
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val resolvedViewer = if (project != null && file != null) {
            ForwardSearchSupport.resolveViewer(project, file, viewer)
        }
        else {
            null
        }
        e.presentation.isEnabledAndVisible =
            file?.fileType is LatexFileType &&
            resolvedViewer?.isAvailable() == true &&
            resolvedViewer.isForwardSearchSupported
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
