package nl.rubensten.texifyidea.structure.bibtex

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiFile
import com.intellij.util.PlatformIcons
import nl.rubensten.texifyidea.psi.BibtexEntry
import nl.rubensten.texifyidea.util.childrenOfType

/**
 * Contains all the entries. Class exist to prevent auto-collapsing.
 *
 * @author Ruben Schellekens
 */
open class BibtexStructureViewEntriesElement(val file: PsiFile) : StructureViewTreeElement, SortableTreeElement {

    val entriesPresentation: ItemPresentation = object : ItemPresentation {

        override fun getLocationString() = file.childrenOfType(BibtexEntry::class).size.toString()

        override fun getPresentableText() = "entries"

        override fun getIcon(b: Boolean) = PlatformIcons.ANNOTATION_TYPE_ICON
    }

    override fun getValue() = file

    override fun navigate(requestFocus: Boolean) = file.navigate(requestFocus)

    override fun canNavigate() = file.canNavigate()

    override fun canNavigateToSource() = file.canNavigateToSource()

    override fun getAlphaSortKey() = "entries"

    override fun getPresentation() = entriesPresentation

    override fun getChildren(): Array<TreeElement> {
        val entries = file.childrenOfType(BibtexEntry::class)
        return entries
                .map { BibtexStructureViewEntryElement(it) }
                .toTypedArray()
    }
}