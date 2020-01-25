package nl.hannahsten.texifyidea.action.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class PreviewToolWindowFactory : ToolWindowFactory, Condition<Project> {
    // Default tool window content
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    }

    // Don't show initially
    override fun value(t: Project?) = false
}