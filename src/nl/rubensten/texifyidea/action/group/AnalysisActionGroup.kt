package nl.rubensten.texifyidea.action.group

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Ruben Schellekens
 */
open class AnalysisActionGroup : DefaultActionGroup() {

    override fun update(event: AnActionEvent) {
        super.update(event)
        event.presentation.icon = TexifyIcons.STATS
    }
}