package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement
import nl.hannahsten.texifyidea.util.Magic

/**
 * @author Hannah Schellekens
 */
class SectionFilter : Filter {

    override fun isVisible(treeElement: TreeElement): Boolean {
        return if (treeElement !is LatexStructureViewCommandElement) {
            true
        }
        else !Magic.Command.sectionMarkers.contains(treeElement.commandName)
    }

    override fun isReverted() = true

    override fun getPresentation() = LatexSectionFilterPresentation

    override fun getName() = "latex.texify.filter.section"

    /**
     * @author Hannah Schellekens
     */
    object LatexSectionFilterPresentation : ActionPresentation {

        override fun getText() = "Show Sectioning"

        override fun getDescription() = "Show Sectioning"

        override fun getIcon() = TexifyIcons.DOT_SECTION!!
    }
}