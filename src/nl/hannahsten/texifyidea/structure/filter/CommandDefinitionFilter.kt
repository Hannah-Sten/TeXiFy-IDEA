package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexOtherCommandPresentation
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * @author Hannah Schellekens
 */
class CommandDefinitionFilter : Filter {

    override fun isVisible(treeElement: TreeElement): Boolean {
        return if (treeElement !is LatexStructureViewCommandElement) {
            true
        }
        else !(
            treeElement.commandName == "\\newcommand" ||
                treeElement.commandName in CommandMagic.mathCommandDefinitions ||
                treeElement.presentation is LatexOtherCommandPresentation
            )
    }

    override fun isReverted() = true

    override fun getPresentation() = LatexNewCommandFilterPresentation

    override fun getName() = "latex.texify.filter.newcommand"

    /**
     * @author Hannah Schellekens
     */
    object LatexNewCommandFilterPresentation : ActionPresentation {

        override fun getText() = "Show Command Definitions"

        override fun getDescription() = "Show command definitions"

        override fun getIcon() = TexifyIcons.DOT_COMMAND
    }
}
