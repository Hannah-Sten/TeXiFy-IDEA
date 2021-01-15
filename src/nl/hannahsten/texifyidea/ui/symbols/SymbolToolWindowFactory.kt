package nl.hannahsten.texifyidea.ui.symbols

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import nl.hannahsten.texifyidea.util.hasLatexModule
import javax.swing.JPanel

/**
 * The Symbol tool window shows an overview of several symbols that can be inserted in the active latex document.
 *
 * @author Hannah Schellekens
 */
open class SymbolToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowPanel = SymbolToolWindow()
        val content = ContentFactory.SERVICE.getInstance().createContent(toolWindowPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun isApplicable(project: Project) = project.hasLatexModule()

    /**
     * The swing contents of the symbol tool window.
     */
    class SymbolToolWindow : JPanel() {

        init {
            add(JBLabel("A label"))
        }
    }
}