package nl.rubensten.texifyidea.gutter

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexBeginCommand
import nl.rubensten.texifyidea.util.TexifyUtil

/**
 * Puts a run-configuration icon in the gutter in front of the \begin{document} command.
 *
 * @author Ruben Schellekens
 */
class LatexCompileGutter : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): RunLineMarkerContributor.Info? {
        element as? LatexBeginCommand ?: return null

        // Break when not a valid command: don't show icon.
        if (!TexifyUtil.isEntryPoint(element)) {
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
