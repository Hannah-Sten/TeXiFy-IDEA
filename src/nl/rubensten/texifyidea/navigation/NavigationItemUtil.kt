package nl.rubensten.texifyidea.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.Magic
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Ruben Schellekens
 */
object NavigationItemUtil {

    @JvmStatic
    fun createNavigationItem(psiElement: PsiElement): NavigationItem? {
        when (psiElement) {
            is LatexCommands -> return GoToSymbolProvider.BaseNavigationItem(psiElement,
                    psiElement.requiredParameter(0) ?: return null,
                    if (psiElement.name in Magic.Command.labels) TexifyIcons.DOT_LABEL else TexifyIcons.DOT_BIB
            )
            is BibtexId -> return GoToSymbolProvider.BaseNavigationItem(psiElement,
                    psiElement.name ?: return null,
                    TexifyIcons.DOT_BIB
            )
        }

        return null
    }
}