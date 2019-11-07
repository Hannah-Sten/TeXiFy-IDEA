package nl.hannahsten.texifyidea.action.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.ui.content.ContentFactory
import nl.hannahsten.texifyidea.action.EditorAction
import nl.hannahsten.texifyidea.ui.EquationPreviewToolWindow
import nl.hannahsten.texifyidea.ui.PreviewFormUpdater
import javax.swing.Icon

/**
 * The [PreviewAction] class is a wrapper around an action that displays a preview to the user.
 *
 * This preview is pretty general, you can display pretty much any part of the user's document, depending
 * on the goal of the implementation. This class has been used to create both the [ShowEquationPreview] Action
 * and the [ShowTikzPreview] Action for example.
 *
 * To create a new Preview Action, simply extend this class and call the [displayPreview] function
 * when appropriate.
 *
 * @author Sergei Izmailov
 * @author FalseHonesty
 */
abstract class PreviewAction(name: String, val icon: Icon?) : EditorAction(name, icon) {

    /**
     * This function is used to display the preview requested as the name suggests.
     *
     * In order to configure the minimal document produced, pass in a lambda to [config].
     * There you can change the document's preamble and content easily.
     *
     * @see [ShowTikzPreview.actionPerformed]
     */
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
                form.compilePreview(element.text)
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
            updater.compilePreview(element.text)
        }
        // Show but not focus the window
        toolWindow.activate(null, false)
    }
}