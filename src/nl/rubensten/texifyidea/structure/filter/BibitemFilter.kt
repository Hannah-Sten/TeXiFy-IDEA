package nl.rubensten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.structure.latex.LatexStructureViewCommandElement

/**
 * @author Ruben Schellekens
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
     * @author Ruben Schellekens
     */
    object BibitemFilterPresentation : ActionPresentation {

        override fun getText() = "Show Bibliography Items"

        override fun getDescription() = "Show Bibliography Items"

        override fun getIcon() = TexifyIcons.DOT_BIB!!
    }
}