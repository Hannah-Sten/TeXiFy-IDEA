package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.hannahsten.texifyidea.run.pdfviewer.sumatra.isSumatraAvailable

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class SumatraActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext) = isSumatraAvailable

    override fun hideIfNoVisibleChildren() = true
}