package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
object NavigationItemUtil {

    fun createSectionMarkerNavigationItem(psiElement: LatexCommands): NavigationItem? {
        val sectionName = psiElement.requiredParameterText(0) ?: return null
        val icon = when (psiElement.commandToken.text) {
            "\\part" -> TexifyIcons.DOT_PART
            "\\chapter" -> TexifyIcons.DOT_CHAPTER
            "\\subsection" -> TexifyIcons.DOT_SUBSECTION
            "\\subsubsection" -> TexifyIcons.DOT_SUBSUBSECTION
            "\\paragraph" -> TexifyIcons.DOT_PARAGRAPH
            "\\subparagraph" -> TexifyIcons.DOT_SUBPARAGRAPH
            // Also catches \section.
            else -> TexifyIcons.DOT_SECTION
        }
        return GoToSymbolProvider.BaseNavigationItem(psiElement, sectionName, icon)
    }
}