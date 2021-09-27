package nl.hannahsten.texifyidea.structure.bibtex

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.util.PlatformIcons
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.*
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class BibtexStructureViewEntryElement(val entry: BibtexEntry) : StructureViewTreeElement, SortableTreeElement {

    private val entryPresentation: ItemPresentation = object : ItemPresentation {

        override fun getLocationString() = when (entry.tokenName()?.toLowerCase()) {
            "string" -> entry.tags().first().content?.text.orEmpty()
            "preamble" -> ""
            else -> entry.tokenName()
        }

        override fun getPresentableText() = when (entry.tokenName()?.toLowerCase()) {
            "preamble" -> "preamble"
            "string" -> entry.tags().firstOrNull()?.keyName()
            else -> entry.identifier()
        } ?: ""

        override fun getIcon(b: Boolean) = when (entry.tokenName()?.toLowerCase()) {
            "string" -> TexifyIcons.STRING
            "preamble" -> PlatformIcons.PROPERTY_ICON
            else -> PlatformIcons.ANNOTATION_TYPE_ICON
        }
    }

    override fun getValue() = entry

    override fun navigate(requestFocus: Boolean) {
        if (entry is NavigationItem) {
            entry.navigate(requestFocus)
        }
    }

    override fun canNavigate() = entry is NavigationItem && entry.canNavigate()

    override fun canNavigateToSource() = entry is NavigationItem && entry.canNavigateToSource()

    override fun getAlphaSortKey() = when (entry.tokenName()) {
        "preamble" -> "a"
        "string" -> "b"
        else -> "c"
    } + entry.identifier()

    override fun getPresentation() = entryPresentation

    override fun getChildren(): Array<TreeElement> {
        if (entry.isString() || entry.isPreamble()) {
            return emptyArray()
        }

        return entry.tags().map { BibtexStructureViewTagElement(it) }.toTypedArray()
    }
}