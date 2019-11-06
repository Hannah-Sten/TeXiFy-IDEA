package nl.hannahsten.texifyidea.action.okular

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import nl.hannahsten.texifyidea.ui.OkularConfigureInverseSearchDialog

class ConfigureInverseSearchAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        OkularConfigureInverseSearchDialog()
    }
}