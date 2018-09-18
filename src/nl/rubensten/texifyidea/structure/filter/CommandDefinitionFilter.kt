package nl.rubensten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.structure.latex.LatexOtherCommandPresentation
import nl.rubensten.texifyidea.structure.latex.LatexStructureViewCommandElement

/**
 * @author Ruben Schellekens
 */
class CommandDefinitionFilter : Filter {

    override fun isVisible(treeElement: TreeElement): Boolean {
        return if (treeElement !is LatexStructureViewCommandElement) {
            true
        }
        else !(treeElement.commandName == "\\newcommand" ||
                treeElement.commandName == "\\DeclareMathOperator" ||
                treeElement.presentation is LatexOtherCommandPresentation)
    }

    override fun isReverted() = true

    override fun getPresentation() = LatexNewCommandFilterPresentation

    override fun getName() = "latex.texify.filter.newcommand"

    /**
     * @author Ruben Schellekens
     */
    object LatexNewCommandFilterPresentation : ActionPresentation {

        override fun getText() = "Show Command Definitions"

        override fun getDescription() = "Show Command Definitions"

        override fun getIcon() = TexifyIcons.DOT_COMMAND!!
    }
}
