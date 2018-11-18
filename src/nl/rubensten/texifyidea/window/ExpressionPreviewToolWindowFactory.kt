package nl.rubensten.texifyidea.window


import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.*
import com.intellij.ui.content.*

/**
 * Created by IntelliJ IDEA.
 * User: Alexey.Chursin
 * Date: Aug 25, 2010
 * Time: 2:09:00 PM
 */
class ExpressionPreviewToolWindowFactory : ToolWindowFactory {
    // Create the tool window content.
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val previewToolWindow = ExpressionPreviewToolWindow(toolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(previewToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}