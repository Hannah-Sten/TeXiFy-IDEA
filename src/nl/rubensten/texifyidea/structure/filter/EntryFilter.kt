package nl.rubensten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.util.PlatformIcons
import nl.rubensten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.rubensten.texifyidea.util.tokenName

/**
 * @author Ruben Schellekens
 */
object EntryFilter : Filter {

    override fun isVisible(treeElement: TreeElement?): Boolean {
        if (treeElement !is BibtexStructureViewEntryElement) {
            return true
        }

        val name = treeElement.entry.tokenName()?.toLowerCase()
        return name == "string" || name == "preamble"
    }

    override fun isReverted() = true

    override fun getName() = "bibtex.texify.filter.entry"

    override fun getPresentation() = object : ActionPresentation {

        override fun getText() = "Show Entries"

        override fun getDescription() = "Show Entries"

        override fun getIcon() = PlatformIcons.ANNOTATION_TYPE_ICON
    }
}