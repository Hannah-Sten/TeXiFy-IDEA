package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewEntryElement
import nl.hannahsten.texifyidea.util.tokenName
import java.util.*

/**
 * @author Hannah Schellekens
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