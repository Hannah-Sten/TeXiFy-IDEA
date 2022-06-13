package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.allFiles
import nl.hannahsten.texifyidea.util.hasLatexModule

class DetexifyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val detexifyToolWindow = DetexifyToolWindow()
        val content = ContentFactory.getInstance().createContent(detexifyToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }

    // Non-idea has no concept of modules so we need to use some other criterion based on the project
    override fun isApplicable(project: Project) =
        if (ApplicationNamesInfo.getInstance().scriptName == "idea") {
        project.hasLatexModule()
    }
    else {
        project.allFiles(LatexFileType).isNotEmpty()
    }

    class DetexifyToolWindow {

        val content = JBCefBrowser(DETEXIFY_URL).component

        companion object {

            const val DETEXIFY_URL = "https://detexify.kirelabs.org/classify.html"
        }
    }
}