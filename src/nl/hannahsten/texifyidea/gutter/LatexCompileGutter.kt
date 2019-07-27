package nl.hannahsten.texifyidea.gutter

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.util.isEntryPoint

/**
 * Puts a run-configuration icon in the gutter in front of the \begin{document} command.
 *
 * @author Hannah Schellekens
 */
class LatexCompileGutter : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): RunLineMarkerContributor.Info? {
        if (element !is LatexBeginCommand) return null

        // Break when not a valid command: don't show icon.
        if (!element.isEntryPoint()) {
            return null
        }

        // Lookup actions.
        val actionManager = ActionManager.getInstance()
        val editConfigs = actionManager.getAction("editRunConfigurations")
        val actions = ExecutorAction.getActions(0)

        // Create icon.
        return RunLineMarkerContributor.Info(
                TexifyIcons.BUILD,
                Function { _ -> "Compile document" },
                actions[0],
                editConfigs
        )
    }
}
