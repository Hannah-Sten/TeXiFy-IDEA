package nl.hannahsten.texifyidea.structure.latex

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.name

/**
 * @author Hannah Schellekens
 */
open class LatexBreadcrumbsInfo : BreadcrumbsProvider {

    override fun getLanguages() = arrayOf(LatexLanguage)

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