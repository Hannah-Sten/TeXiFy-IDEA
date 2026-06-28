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

        try {
            val resolvedViewer = ForwardSearchSupport.performForwardSearch(project, file, textEditor.editor, fallbackViewer = viewer)
            if (resolvedViewer != null) this.viewer = resolvedViewer
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

        e.presentation.isEnabledAndVisible =
            project != null &&
            file?.fileType is LatexFileType &&
            ForwardSearchSupport.canForwardSearch(project, file, viewer)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
