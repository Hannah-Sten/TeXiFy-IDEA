package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * @author Hannah Schellekens
 */
class SectionFilter : Filter {

    override fun isVisible(treeElement: TreeElement): Boolean = if (treeElement !is LatexStructureViewCommandElement) {
        true
    }
    else !CommandMagic.sectionNameToLevel.contains(treeElement.commandName)

    override fun isReverted() = true

    override fun getPresentation() = LatexSectionFilterPresentation

    override fun getName() = "latex.texify.filter.section"

    /**
     * @author Hannah Schellekens
     */
    object LatexSectionFilterPresentation : ActionPresentation {

        override fun getText() = "Show Sectioning"

        override fun getDescription() = "Show sectioning"

        override fun getIcon() = TexifyIcons.DOT_SECTION
    }
}