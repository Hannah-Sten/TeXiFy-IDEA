package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.forcedFirstRequiredParameterAsCommand
import nl.hannahsten.texifyidea.util.requiredParameter

/**
 * @author Hannah Schellekens
 */
object NavigationItemUtil {

    @JvmStatic
    fun createLabelNavigationItem(psiElement: PsiElement): NavigationItem? {
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

    @JvmStatic
    fun createCommandDefinitionNavigationItem(psiElement: LatexCommands): NavigationItem? {
        val defined = psiElement.forcedFirstRequiredParameterAsCommand() ?: return null
        return GoToSymbolProvider.BaseNavigationItem(psiElement, defined.name ?: "", TexifyIcons.DOT_COMMAND)
    }

    @JvmStatic
    fun createEnvironmentDefinitionNavigationItem(psiElement: LatexCommands): NavigationItem? {
        val environmentName = psiElement.requiredParameter(0) ?: return null
        return GoToSymbolProvider.BaseNavigationItem(psiElement, environmentName, TexifyIcons.DOT_ENVIRONMENT)
    }

    @JvmStatic
    fun createSectionMarkerNavigationItem(psiElement: LatexCommands): NavigationItem? {
        val sectionName = psiElement.requiredParameter(0) ?: return null
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