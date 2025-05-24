package nl.hannahsten.texifyidea.structure.latex

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import nl.hannahsten.texifyidea.editor.folding.LatexSectionFoldingBuilder
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
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
        is LatexCommands -> if (element.name in CommandMagic.sectioningCommands.map { it.cmd }) element.requiredParameter(0) ?: element.name else element.name
        else -> ""
    } ?: ""

    override fun acceptElement(element: PsiElement) = when (element) {
        is LatexEnvironment -> true
        is LatexCommands -> true
        else -> false
    }

    override fun getParent(element: PsiElement): PsiElement? {
        val document = element.containingFile.document() ?: return super.getParent(element)
        // Add sections
        val parent = LatexSectionFoldingBuilder().buildFoldRegions(element.containingFile, document, quick = true)
            .filter { descriptor ->
                val range = descriptor.range
                val elementRange = element.textRange ?: return@filter false
                val parentRange = element.parent?.textRange ?: return@filter false
                // Only top-level elements in the section should have the section as parents, other elements should keep their direct parent (e.g. an environment)
                range.contains(elementRange) && (!range.contains(parentRange)) &&
                    // Avoid creating a loop
                    descriptor.element.psi != element &&
                    descriptor.element.psi?.parents()?.contains(element) != true
            }
            .minByOrNull { it.range.length }
            ?.element?.psi
        return parent ?: super.getParent(element)
    }
}