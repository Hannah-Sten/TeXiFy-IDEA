package nl.hannahsten.texifyidea.structure.latex

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import nl.hannahsten.texifyidea.editor.folding.LatexSectionFoldingBuilder
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.parser.name
import nl.hannahsten.texifyidea.util.parser.parents
import nl.hannahsten.texifyidea.util.parser.requiredParameter

/**
 * @author Hannah Schellekens
 */
open class LatexBreadcrumbsInfo : BreadcrumbsProvider {

    override fun getLanguages() = arrayOf(LatexLanguage)

    override fun getElementInfo(element: PsiElement) = when (element) {
        is LatexEnvironment -> element.name()?.text
        is LatexCommands -> if (element.name == "\\section") element.requiredParameter(0) else  element.name
        else -> ""
    } ?: ""

    override fun acceptElement(element: PsiElement) = when (element) {
        is LatexEnvironment -> true
        is LatexCommands -> true
        else -> false
    }

    override fun getParent(element: PsiElement): PsiElement? {
        val document = element.containingFile.document() ?: return super.getParent(element)
        val parent = LatexSectionFoldingBuilder().buildFoldRegions(element.containingFile, document, quick = true)
            // Only top-level elements in the section should have the section as parents, other elements should keep their direct parent (e.g. an environment)
            .filter { it.range.contains(element.textRange) }
            .filterNot { it.range.contains(element.parent.textRange) }
            .firstOrNull { it.element.psi != element }
            ?.element?.psi
        // Avoid creating a loop
        if (parent?.parents()?.contains(element) == true) { return super.getParent(element) }
        return parent ?: super.getParent(element)
    }
}