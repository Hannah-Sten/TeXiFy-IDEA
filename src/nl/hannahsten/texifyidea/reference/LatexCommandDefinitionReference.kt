package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.project.DumbService
import com.intellij.psi.*
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.NewDefinitionIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.definitionCommand

/**
 * Command reference. When resolved, points to the command definition.
 *
 * @author Abby Berkers
 */
class LatexCommandDefinitionReference(element: LatexCommands) : PsiReferenceBase<LatexCommands>(element), PsiPolyVariantReference {

    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    private fun resolveWithDefIndex(name: String): Array<ResolveResult> {
        val file = element.containingFile
        if (DumbService.isDumb(file.project)) return ResolveResult.EMPTY_ARRAY
        val definitions = NewDefinitionIndex.getByNameInFileSet(name, file)
        return definitions.mapNotNull { newcommand ->
            // Find the command being defined, e.g. \hi in case of \newcommand{\hi}{}
            // We should resolve to \hi, not to \newcommand, because otherwise the find usages will try to find references to the \hi definition and won't find anything because the references point to the \newcommand
            val definedCommand = newcommand.definitionCommand()
            definedCommand?.let { PsiElementResolveResult(definedCommand) }
        }.toTypedArray()
    }

    private fun resolveWithDefinitionService(name: String): PsiElement? {
        val element = myElement
        val file = element.containingFile
        val virtualFile = file.virtualFile ?: return null
        val project = element.project
        if (DumbService.isDumb(project)) return null // If the project is dumb, we cannot resolve definitions, so return null
        val defService = LatexDefinitionService.getInstance(project)
        return defService.resolveCommandDef(virtualFile, name)?.definitionCommandPointer?.element?.definitionCommand()
    }

    // Find all command definitions and redefinitions which define the current element
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        // Don't resolve to a definition when you are in a \newcommand,
        // and if this element is the element that is being defined
        val name = element.name ?: return ResolveResult.EMPTY_ARRAY
        if (LatexPsiUtil.isCommandBeingDefined(element)) return ResolveResult.EMPTY_ARRAY

        resolveWithDefinitionService(name)?.let { return arrayOf(PsiElementResolveResult(it)) }
        return resolveWithDefIndex(name)
    }

    override fun resolve(): PsiElement? {
        val name = element.name ?: return null
        if (LatexPsiUtil.isCommandBeingDefined(element)) return null
        // First try to resolve with the new definition index
        resolveWithDefinitionService(name)?.let { return it }
        // If that didn't work, try the new definition index
        val results = resolveWithDefIndex(name)
        if(results.size == 1) {
            return results[0].element
        }
        return null
    }

    // Check if this reference resolves to the given element
    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element !is LatexCommands) return false
        if (this.element.name != element.name) return false
        return multiResolve(false).any { it.element == element }
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        myElement.setName(newElementName)
        return myElement
    }
}