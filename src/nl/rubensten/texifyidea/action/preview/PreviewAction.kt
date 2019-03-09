package nl.rubensten.texifyidea.action.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.ui.content.ContentFactory
import nl.rubensten.texifyidea.action.EditorAction
import nl.rubensten.texifyidea.ui.EquationPreviewToolWindow
import nl.rubensten.texifyidea.ui.PreviewFormUpdater
import javax.swing.Icon

abstract class PreviewAction(name: String?, val icon: Icon?) : EditorAction(name, icon) {
    protected fun displayPreview(
        project: Project,
        element: PsiElement,
        key: Key<PreviewFormUpdater>,
        config: PreviewFormUpdater.() -> Unit
    ) {
        val toolWindowId = name
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindowIcon = icon

        val toolWindow = toolWindowManager.getToolWindow(toolWindowId)
            ?: toolWindowManager.registerToolWindow(toolWindowId, true, ToolWindowAnchor.BOTTOM)
                .apply { icon = toolWindowIcon }

        val containingFile = element.containingFile
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val document = psiDocumentManager.getDocument(containingFile)
        val textOffset = element.textOffset
        val lineNumber = document?.getLineNumber(textOffset) ?: 0
        val displayName = "${containingFile.name}:${lineNumber + 1}"

        val contentCount = toolWindow.contentManager.contentCount

        var replaced = false
        for (i in 0 until contentCount) {
            val content = toolWindow.contentManager.getContent(i) ?: continue
            if (!content.isPinned) {
                val form = content.getUserData(key) ?: continue
                form.setPreviewCode(element.text)
                content.displayName = displayName
                replaced = true
                break
            }
        }

        val contentFactory = ContentFactory.SERVICE.getInstance()

        if (!replaced) {
            val previewToolWindow = EquationPreviewToolWindow()
            val newContent = contentFactory.createContent(
                previewToolWindow.content,
                displayName,
                true
            )
            toolWindow.contentManager.addContent(newContent)
            val updater = PreviewFormUpdater(previewToolWindow.form)

            updater.config()

            newContent.putUserData(key, updater)
            updater.setPreviewCode(element.text)
        }
        toolWindow.activate(null)
    }
}