package nl.rubensten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import nl.rubensten.texifyidea.run.isSumatraAvailable

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class SumatraActionGroup : DefaultActionGroup(), DumbAware {

    override fun canBePerformed(context: DataContext) = isSumatraAvailable

    override fun hideIfNoVisibleChildren() = true
}