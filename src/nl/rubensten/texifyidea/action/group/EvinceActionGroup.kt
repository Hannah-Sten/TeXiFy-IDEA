package nl.rubensten.texifyidea.action.group

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.run.evince.isEvinceAvailable

class EvinceActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext): Boolean {
        return isEvinceAvailable()
    }

    override fun hideIfNoVisibleChildren(): Boolean = true

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.icon = TexifyIcons.PDF_FILE // todo
    }
}