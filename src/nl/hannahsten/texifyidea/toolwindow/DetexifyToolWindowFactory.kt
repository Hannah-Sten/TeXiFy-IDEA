package nl.hannahsten.texifyidea.toolwindow

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import nl.hannahsten.texifyidea.modules.LatexModuleType
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

class DetexifyToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = DetexifyToolWindow(toolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content: Content = contentFactory.createContent(myToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun isApplicable(project: Project): Boolean {
        return ModuleManager.getInstance(project).modules.any { it.moduleTypeName == LatexModuleType.ID }
    }
}

class DetexifyToolWindow(window: ToolWindow) {
    @JvmField
    val browser = JBCefBrowser("https://detexify.kirelabs.org/classify.html")
    val content = browser.component

    init {
        window.title = "Detexify"
        window.component.addFocusListener(object : FocusListener {
            override fun focusGained(focusEvent: FocusEvent) {
                for (listener in browser.component.focusListeners) listener.focusGained(focusEvent)
            }

            override fun focusLost(focusEvent: FocusEvent) {
                for (listener in browser.component.focusListeners) listener.focusLost(focusEvent)
            }
        })
    }
}