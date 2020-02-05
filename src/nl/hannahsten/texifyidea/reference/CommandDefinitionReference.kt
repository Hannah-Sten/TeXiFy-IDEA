package nl.hannahsten.texifyidea.reference

import com.intellij.psi.*
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.projectSearchScope

/**
 * Command reference. When resolved, points to the command definition.
 *
 * @author Abby Berkers
 */
class CommandDefinitionReference(element: LatexCommands) : PsiReferenceBase<LatexCommands>(element), PsiPolyVariantReference {
    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    // Find all command definitions and redefinitions which define the current element
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return LatexDefinitionIndex.getCommandsByNames(Magic.Command.commandDefinitions, element.project, element.project.projectSearchScope)
                .filter { it.requiredParameters.firstOrNull() == element.name }
                .map { PsiElementResolveResult(it) }
                .toArray(emptyArray())

    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    // Check if this reference resolves to the given element
    override fun isReferenceTo(element: PsiElement): Boolean {
        return multiResolve(false).any { it.element == element }
    }
}