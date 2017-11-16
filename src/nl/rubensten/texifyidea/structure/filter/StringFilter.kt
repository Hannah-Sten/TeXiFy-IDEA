package nl.rubensten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.rubensten.texifyidea.util.tokenName

/**
 * @author Ruben Schellekens
 */
object StringFilter : Filter {

    override fun isVisible(treeElement: TreeElement?): Boolean {
        if (treeElement !is BibtexStructureViewEntryElement) {
            return true
        }

        return treeElement.entry.tokenName()?.toLowerCase() != "string"
    }

    override fun isReverted() = true

    override fun getName() = "bibtex.texify.filter.string"

    override fun getPresentation() = object : ActionPresentation {

        override fun getText() = "Show @string"

        override fun getDescription() = "Show @string"

        override fun getIcon() = TexifyIcons.STRING
    }
}