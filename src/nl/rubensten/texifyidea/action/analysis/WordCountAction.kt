package nl.rubensten.texifyidea.action.analysis

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import nl.rubensten.texifyidea.util.debugln

/**
 * @author Ruben Schellekens
 */
class WordCountAction : AnAction(
        "Word Count",
        "Estimate the word count of the currently active .tex file and inclusions.",
        null
) {

    override fun actionPerformed(event: AnActionEvent?) {
        val virtualFile = event?.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(PlatformDataKeys.PROJECT)

        virtualFile.name debugln "Opened file"
    }
}