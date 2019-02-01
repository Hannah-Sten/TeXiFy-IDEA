package nl.rubensten.texifyidea.action.group

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.rubensten.texifyidea.util.isLatexFile

/**
 * @author Ruben Schellekens
 */
open class LatexAnalyzeMenuGroup : DefaultActionGroup() {

    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.PSI_FILE)
        event.presentation.isEnabledAndVisible = file?.isLatexFile() ?: false
    }
}