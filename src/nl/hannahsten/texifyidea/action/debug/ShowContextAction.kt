package nl.hannahsten.texifyidea.action.debug

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationsManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.platform.ide.progress.ModalTaskOwner.project
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

class ShowContextAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // get the active editor
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
        // get the file from the editor
        val file = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE)
            ?: return
        // get the caret position
        val caret = editor.caretModel.currentCaret
        // get the offset of the caret
        val offset = caret.offset
        // get the element at the caret position
        val element = file.findElementAt(offset) ?: return
        // show the context of the element in a dialog
        val project = e.project ?: return
        val semanticLookup = LatexDefinitionService.getInstance(e.project!!).getFilesetBundles(file.virtualFile).firstOrNull() ?: return
        val contexts = LatexPsiUtil.resolveContextUpward(element, semanticLookup)
        // pop up the information
        Notification("LaTeX",
            contexts.toString(),
            NotificationType.INFORMATION).notify(project)
    }
}