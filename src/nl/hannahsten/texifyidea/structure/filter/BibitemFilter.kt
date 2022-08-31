package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement

/**
 * @author Hannah Schellekens
 */
class BibitemFilter : Filter {

    override fun isVisible(treeElement: TreeElement): Boolean {
        return if (treeElement !is LatexStructureViewCommandElement) {
            true
        }
        else treeElement.commandName != "\\bibitem"
    }

    override fun isReverted() = true

    override fun getPresentation() = BibitemFilterPresentation

    override fun getName() = "latex.texify.filter.bibitem"

    /**
     * @author Hannah Schellekens
     */
    object BibitemFilterPresentation : ActionPresentation {

        override fun getText() = "Show Bibliography Items"

        override fun getDescription() = "Show bibliography items"

        override fun getIcon() = TexifyIcons.DOT_BIB
    }
}