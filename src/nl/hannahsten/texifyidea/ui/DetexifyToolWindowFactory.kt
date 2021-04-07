package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import nl.hannahsten.texifyidea.modules.LatexModuleType

class DetexifyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val detexifyToolWindow = DetexifyToolWindow()
        val content = ContentFactory.SERVICE.getInstance().createContent(detexifyToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }

    /**
     * Only show the Detexify tool window in a project that contains a LaTeX module.
     */
    override fun isApplicable(project: Project): Boolean {
        return ModuleManager.getInstance(project).modules.any { it.moduleTypeName == LatexModuleType.ID }
    }

    class DetexifyToolWindow {

        val content = JBCefBrowser(DETEXIFY_URL).component

        companion object {

            const val DETEXIFY_URL = "https://detexify.kirelabs.org/classify.html"
        }
    }
}