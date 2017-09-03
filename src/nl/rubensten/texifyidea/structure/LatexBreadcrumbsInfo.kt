package nl.rubensten.texifyidea.structure

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.util.name

/**
 * @author Ruben Schellekens
 */
open class LatexBreadcrumbsInfo : BreadcrumbsProvider {

    override fun getLanguages() = arrayOf(LatexLanguage.INSTANCE)

    override fun getElementInfo(element: PsiElement) = when (element) {
        is LatexEnvironment -> element.name()?.text
        is LatexCommands -> element.commandToken.text
        else -> ""
    } ?: ""

    override fun acceptElement(element: PsiElement) = when (element) {
        is LatexEnvironment -> true
        is LatexCommands -> true
        else -> false
    }
}