package nl.hannahsten.texifyidea.gutter

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.isEntryPoint
import nl.hannahsten.texifyidea.util.parentOfType

/**
 * Puts a run-configuration icon in the gutter in front of the \begin{document} command.
 *
 * @author Hannah Schellekens
 */
class LatexCompileGutter : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        // Only show the icon on lines with `\begin` => show only one icon.
        if (element.node.elementType != LatexTypes.BEGIN_TOKEN) return null

        // Find the total enclosed begin command: required to easily find the first command.
        val beginCommand = element.parentOfType(LatexBeginCommand::class) ?: return null

        // Break when not a valid command: don't show icon.
        if (beginCommand.isEntryPoint().not()) return null

        // Lookup actions.
        val actionManager = ActionManager.getInstance()
        val editConfigs = actionManager.getAction("editRunConfigurations")
        val actions = ExecutorAction.getActions(0)

        // Create icon.
        return Info(
            TexifyIcons.BUILD,
            Function { "Compile document" },
            actions[0],
            editConfigs
        )
    }
}