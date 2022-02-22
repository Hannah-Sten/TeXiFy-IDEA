package nl.hannahsten.texifyidea.structure.bibtex

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.BibtexFile
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class BibtexStructureViewElement(val element: PsiElement) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue() = element

    override fun navigate(requestFocus: Boolean) {
        if (element is NavigationItem) {
            element.navigate(requestFocus)
        }
    }

    override fun canNavigate() = element is NavigationItem && element.canNavigate()

    override fun canNavigateToSource() = element is NavigationItem && element.canNavigateToSource()

    override fun getAlphaSortKey() = when (element) {
        is PsiFile -> element.name.lowercase(Locale.getDefault())
        else -> element.text.lowercase(Locale.getDefault())
    }

    override fun getPresentation(): ItemPresentation {
        if (element is BibtexFile) {
            return BibtexFilePresentation(element)
        }

        throw AssertionError("Should not happen: element !is BibtexFile.")
    }

    override fun getChildren(): Array<TreeElement> {
        if (element !is BibtexFile) {
            return emptyArray()
        }

        return arrayOf(BibtexStructureViewEntriesElement(element))
    }
}