package nl.hannahsten.texifyidea.structure.latex

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexComposite
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.psi.traverseContextualSiblingsPrev
import nl.hannahsten.texifyidea.util.parser.requiredParameter

/**
 *
 *
 * @author Li Ernest, Hannah Schellekens
 */
class LatexBreadcrumbsInfo : BreadcrumbsProvider {

    override fun getLanguages() = arrayOf(LatexLanguage)

    override fun getElementInfo(element: PsiElement) = when (element) {
        is LatexEnvironment -> element.getEnvironmentName()
        is LatexCommands ->
            if (element.name in CommandMagic.sectionNameToLevel) {
                element.requiredParameter(0) ?: element.name
            }
            else {
                element.name
            }

        else -> ""
    } ?: ""

    override fun acceptElement(element: PsiElement) = when (element) {
        is LatexEnvironment -> true
        is LatexCommands -> true
        else -> false
    }

    /**
     * Find the previous sibling command that is a section marker.
     * If it exists, return it, otherwise return null.
     */
    private fun findParentSibling(element: PsiElement, originLevel: Int): PsiElement? {
        /*
        Notice that the element is surrounded by `no_math_content`, so we should go an extra level up
        The direct sibling
         */

        element.traverseContextualSiblingsPrev {
            if (it is LatexCommands) {
                val prevLevel = CommandMagic.sectionNameToLevel[it.name]
                if (prevLevel != null && prevLevel < originLevel) {
                    return it
                }
            }
        }
        return null
    }

    override fun getParent(element: PsiElement): PsiElement? {
        /*
        if it has a previous sibling command that is a section marker that is of lower level, then the parent is the command
        If the parent is a LatexEnvironment or LatexCommands, return it
        Otherwise, goto the parent
         */

        val level = (element as? LatexCommands)?.let { cmd ->
            CommandMagic.sectionNameToLevel[cmd.name]
        } ?: Int.MAX_VALUE

        var current: PsiElement = element
        while (true) {
            val res = findParentSibling(current, level)
            res?.let { return it }
            val parent = current.parent ?: return null
            if (parent is LatexEnvironment || parent is LatexCommands || parent !is LatexComposite) {
                return parent
            }
            current = parent
        }
    }
}