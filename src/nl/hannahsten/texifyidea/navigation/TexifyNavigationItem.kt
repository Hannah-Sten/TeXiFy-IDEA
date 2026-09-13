package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Copied from https://github.com/JetBrains/intellij-community/blob/c11d6a0418aa5652be69f46b007c61ccfa1c2350/xml/dom-impl/src/com/intellij/util/xml/model/gotosymbol/GoToSymbolProvider.java#L103
 */
class TexifyNavigationItem(
    private val element: PsiElement,
    private val name: String,
    private val icon: Icon
) : NavigationItem {
    override fun getName(): String = name
    override fun getPresentation(): ItemPresentation = object : ItemPresentation {
        override fun getPresentableText(): String = name
        override fun getIcon(unused: Boolean): Icon = icon
    }
    override fun navigate(requestFocus: Boolean) = (element as? com.intellij.pom.Navigatable)?.navigate(requestFocus) ?: Unit
    override fun canNavigate(): Boolean = (element as? com.intellij.pom.Navigatable)?.canNavigate() == true
    override fun canNavigateToSource(): Boolean = (element as? com.intellij.pom.Navigatable)?.canNavigateToSource() == true
}
