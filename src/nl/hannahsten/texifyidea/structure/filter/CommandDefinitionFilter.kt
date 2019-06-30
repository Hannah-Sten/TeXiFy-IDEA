package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexOtherCommandPresentation
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement

/**
 * @author Hannah Schellekens
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
     * @author Hannah Schellekens
     */
    object LatexNewCommandFilterPresentation : ActionPresentation {

        override fun getText() = "Show Command Definitions"

        override fun getDescription() = "Show Command Definitions"

        override fun getIcon() = TexifyIcons.DOT_COMMAND!!
    }
}
