package nl.rubensten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.rubensten.texifyidea.run.sumatra.isSumatraAvailable

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class SumatraActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext) = isSumatraAvailable

    override fun hideIfNoVisibleChildren() = true
}