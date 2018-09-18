package nl.rubensten.texifyidea.structure.filter

import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import javax.swing.Icon

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

        override fun getPresentableText(): String? = null

        override fun getLocationString(): String? = null

        override fun getIcon(b: Boolean): Icon? = null
    }
}
