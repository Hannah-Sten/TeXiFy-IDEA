package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import javax.swing.Icon

/**
 * @author Hannah Schellekens
 */
class LabelFilter : Filter {

    override fun isVisible(treeElement: TreeElement): Boolean {
        if (treeElement !is LatexStructureViewCommandElement) {
            return true
        }
        return !getLabelDefinitionCommands().contains(treeElement.commandName)
    }

    override fun isReverted(): Boolean = true

    override fun getPresentation(): ActionPresentation = LatexLabelFilterPresentation.INSTANCE

    override fun getName(): String = "latex.texify.filter.label"

    /**
     * @author Hannah Schellekens
     */
    private class LatexLabelFilterPresentation : ActionPresentation {

        override fun getText(): String = "Show Labels"

        override fun getDescription(): String = "Show labels"

        override fun getIcon(): Icon = TexifyIcons.DOT_LABEL

        companion object {

            val INSTANCE = LatexLabelFilterPresentation()
        }
    }
}