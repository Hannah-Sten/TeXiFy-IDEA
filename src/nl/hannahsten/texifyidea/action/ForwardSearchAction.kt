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
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.util.latexTemplateRunConfig
import nl.hannahsten.texifyidea.util.selectedRunConfig

open class ForwardSearchAction(var viewer: PdfViewer? = null) : EditorAction(
    name = "_Forward Search"
) {

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        if (viewer?.isAvailable() != true || file.fileType !is LatexFileType) return

        val document = textEditor.editor.document
        val line = document.getLineNumber(textEditor.editor.caretModel.offset) + 1
        try {
            viewer?.forwardSearch(null, file.path, line, project, focusAllowed = true)
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
        e.presentation.isEnabledAndVisible = (
            e.project?.selectedRunConfig()?.pdfViewer == viewer
                || (e.project?.selectedRunConfig() == null && e.project?.latexTemplateRunConfig()?.pdfViewer == viewer)
            ) && e.getData(CommonDataKeys.VIRTUAL_FILE)?.fileType is LatexFileType
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
