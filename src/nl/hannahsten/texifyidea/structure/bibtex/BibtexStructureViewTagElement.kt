package nl.hannahsten.texifyidea.structure.bibtex

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.util.PlatformIcons
import nl.hannahsten.texifyidea.psi.BibtexTag
import nl.hannahsten.texifyidea.util.evaluate
import nl.hannahsten.texifyidea.util.keyName

/**
 * @author Hannah Schellekens
 */
open class BibtexStructureViewTagElement(val tag: BibtexTag) : StructureViewTreeElement, SortableTreeElement {

    private val tagPresentation: PresentationData = object : PresentationData() {

        override fun getLocationString() = tag.content?.evaluate().orEmpty()

        override fun getPresentableText() = tag.keyName()

        override fun getIcon(b: Boolean) = PlatformIcons.PROTECTED_ICON
    }

    override fun getValue() = tag

    override fun navigate(requestFocus: Boolean) {
        if (tag is NavigationItem) {
            tag.navigate(requestFocus)
        }
    }

    override fun canNavigate() = tag is NavigationItem && tag.canNavigate()

    override fun canNavigateToSource() = tag is NavigationItem && tag.canNavigateToSource()

    override fun getAlphaSortKey() = tag.keyName() ?: ""

    override fun getPresentation() = tagPresentation

    override fun getChildren(): Array<TreeElement> = emptyArray()
}