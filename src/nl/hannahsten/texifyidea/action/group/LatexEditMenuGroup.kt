package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.hannahsten.texifyidea.util.files.isLatexFile

/**
 * @author Hannah Schellekens
 */
open class LatexEditMenuGroup : DefaultActionGroup() {

    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.PSI_FILE)
        event.presentation.isEnabledAndVisible = file?.isLatexFile() ?: false
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}