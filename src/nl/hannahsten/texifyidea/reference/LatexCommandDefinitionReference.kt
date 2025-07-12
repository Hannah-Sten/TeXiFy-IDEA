package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.project.DumbService
import com.intellij.psi.*
import com.intellij.util.indexing.DumbModeAccessType
import nl.hannahsten.texifyidea.index.NewDefinitionIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.isInsideDefinition
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

    // Find all command definitions and redefinitions which define the current element
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        // Don't resolve to a definition when you are in a \newcommand,
        // and if this element is the element that is being defined
        val element = myElement
        if (element.isInsideDefinition()) {
            return ResolveResult.EMPTY_ARRAY
        }
        val name = element.name ?: return ResolveResult.EMPTY_ARRAY
        val file = element.containingFile
        if(DumbService.isDumb(file.project)) return ResolveResult.EMPTY_ARRAY
        val definitions = NewDefinitionIndex.getByNameInFileSet(name, file)
        return definitions.mapNotNull { newcommand ->
            // Find the command being defined, e.g. \hi in case of \newcommand{\hi}{}
            // We should resolve to \hi, not to \newcommand, because otherwise the find usages will try to find references to the \hi definition and won't find anything because the references point to the \newcommand
            val definedCommand = newcommand.definitionCommand()
            definedCommand?.let { PsiElementResolveResult(definedCommand) }
        }.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
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