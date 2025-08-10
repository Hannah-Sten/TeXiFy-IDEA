package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.showOkCancelDialog
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.isLatexProject

class DetexifyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val detexifyToolWindow = DetexifyToolWindow()

        val hasApprovedDetexify = TexifySettings.getInstance().hasApprovedDetexify

        // If not approved, ask for approval every time, otherwise remember it
        if (!hasApprovedDetexify) {
            val result = showOkCancelDialog("Open External Website", "You are about to open an external website: ${DetexifyToolWindow.DETEXIFY_URL}. This website may serve ads. Do you want to continue?", "Ok", "Cancel")
            if (result != Messages.OK) {
                return
            }
            else {
                TexifySettings.getInstance().hasApprovedDetexify = true
            }
        }

        val content = ContentFactory.getInstance().createContent(detexifyToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }

    // Non-idea has no concept of modules so we need to use some other criterion based on the project
    override suspend fun isApplicableAsync(project: Project) = smartReadAction(project) { project.isLatexProject() }

    class DetexifyToolWindow {

        val content = JBCefBrowser(DETEXIFY_URL).component

        companion object {

            const val DETEXIFY_URL = "https://detexify.kirelabs.org/classify.html"
        }
    }
}