package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement
import nl.hannahsten.texifyidea.util.getIncludeCommands

/**
 * @author Hannah Schellekens
 */
class IncludesFilter : Filter {

    override fun isVisible(treeElement: TreeElement): Boolean {
        return if (treeElement !is LatexStructureViewCommandElement) {
            true
        }
        else treeElement.commandName !in getIncludeCommands()
    }

    override fun isReverted() = true

    override fun getPresentation() = LatexIncludesFilterPresentation

    override fun getName() = "latex.texify.filter.includes"

    /**
     * @author Hannah Schellekens
     */
    object LatexIncludesFilterPresentation : ActionPresentation {

        override fun getText() = "Show Includes"

        override fun getDescription() = "Show includes"

        override fun getIcon() = TexifyIcons.DOT_INCLUDE
    }
}
