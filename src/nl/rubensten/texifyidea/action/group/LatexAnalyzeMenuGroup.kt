package nl.rubensten.texifyidea.action.group

import com.intellij.openapi.actionSystem.*
import nl.rubensten.texifyidea.action.analysis.WordCountAction

/**
 * @author Ruben Schellekens
 */
open class LatexAnalyzeMenuGroup : DefaultActionGroup() {


    override fun update(event: AnActionEvent) {

        val file = event.getData(CommonDataKeys.PSI_FILE)
        event.presentation.isEnabledAndVisible = true
//        event.presentation.isEnabledAndVisible = file?.isLatexFile() ?: false
    }
}