package nl.rubensten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.rubensten.texifyidea.run.evince.isEvinceAvailable

class EvinceActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext) = isEvinceAvailable()

    override fun hideIfNoVisibleChildren(): Boolean = true
}