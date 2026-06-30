package nl.hannahsten.texifyidea.intentions

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.run.pdfviewer.ForwardSearchSupport

// A before and after template is expected for the intention.
// However, forward search doesn't have a before/after preview.
@Suppress("IntentionDescriptionNotFoundInspection")
class LatexForwardSearchIntention : TexifyIntentionBase("Forward search in PDF") {

    override fun generatePreview(project: Project, editor: Editor, psiFile: PsiFile): IntentionPreviewInfo = IntentionPreviewInfo.EMPTY

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        // since isAvailable should be inexpensive, we only check the file type
        // without looking for a supported viewer.
//        return editor != null &&
//                file != null &&
//                file.fileType is LatexFileType &&
//                file.virtualFile != null

        if (editor == null || file == null || file.fileType !is LatexFileType) return false
        val virtualFile = file.virtualFile ?: return false

        return ForwardSearchSupport.canForwardSearch(project, virtualFile, getForwardSearchAction()?.viewer)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || file.fileType !is LatexFileType) return
        val virtualFile = file.virtualFile ?: return

        val forwardAction = getForwardSearchAction()

        try {
            val resolvedViewer = ForwardSearchSupport.performForwardSearch(project, virtualFile, editor, fallbackViewer = forwardAction?.viewer)
            if (resolvedViewer != null) forwardAction?.viewer = resolvedViewer
        }
        catch (e: TeXception) {
            // Show a notification if the forward search fails, but only catch TeXception and let other unexpected exceptions bubble up.
            Notification(
                "LaTeX", "Forward search error", "${e.message}",
                NotificationType.WARNING
            ).notify(project)
        }
    }

    private fun getForwardSearchAction() = ActionManager.getInstance()
        .getAction("texify.ForwardSearch") as? ForwardSearchAction
}