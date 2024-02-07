package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Hannah Schellekens
 */
open class InsertFontStyleActionGroup : DefaultActionGroup() {

    override fun update(event: AnActionEvent) {
        super.update(event)
        event.presentation.icon = TexifyIcons.FONT_BOLD
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
