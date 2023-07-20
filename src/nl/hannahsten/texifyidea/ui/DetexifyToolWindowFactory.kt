package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.util.isLatexProject
import javax.swing.Icon

class DetexifyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val detexifyToolWindow = DetexifyToolWindow()
        val content = ContentFactory.getInstance().createContent(detexifyToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }

    // Non-idea has no concept of modules so we need to use some other criterion based on the project
    override fun isApplicable(project: Project) = project.isLatexProject()

    class DetexifyToolWindow {

        val content = JBCefBrowser(DETEXIFY_URL).component

        companion object {

            const val DETEXIFY_URL = "https://detexify.kirelabs.org/classify.html"
        }
    }
}