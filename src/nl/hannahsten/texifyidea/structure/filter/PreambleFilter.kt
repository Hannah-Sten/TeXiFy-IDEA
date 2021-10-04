package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.util.PlatformIcons
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.util.tokenName
import java.util.*

/**
 * @author Hannah Schellekens
 */
object PreambleFilter : Filter {

    override fun isVisible(treeElement: TreeElement?): Boolean {
        if (treeElement !is BibtexStructureViewEntryElement) {
            return true
        }

        return treeElement.entry.tokenName()?.toLowerCase() != "preamble"
    }

    override fun isReverted() = true

    override fun getName() = "bibtex.texify.filter.preamble"

    override fun getPresentation() = object : ActionPresentation {

        override fun getText() = "Show @preamble"

        override fun getDescription() = "Show @preamble"

        override fun getIcon() = PlatformIcons.PROPERTY_ICON
    }
}