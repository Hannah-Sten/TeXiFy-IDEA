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
class EquationPreviewToolWindowFactory : ToolWindowFactory {
    // Create the tool window content.
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = EquationPreviewToolWindow(toolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(myToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}