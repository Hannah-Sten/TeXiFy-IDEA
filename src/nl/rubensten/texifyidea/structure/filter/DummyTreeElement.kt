package nl.rubensten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation

/**
 * @author Ruben Schellekens
 */
enum class DummyTreeElement : TreeElement {

    INSTANCE;

    override fun getPresentation() = DummyPresentation

    override fun getChildren(): Array<out TreeElement> = TreeElement.EMPTY_ARRAY

    /**
     * @author Ruben Schellekens
     */
    object DummyPresentation : ItemPresentation {

        override fun getPresentableText() = null

        override fun getLocationString() = null

        override fun getIcon(b: Boolean) = null
    }
}
