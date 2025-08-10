package nl.hannahsten.texifyidea.reference

import com.intellij.openapi.project.DumbService
import com.intellij.psi.*
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

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
//        return super.m
//
//        val name = element.name ?: return ResolveResult.EMPTY_ARRAY
//        val file = element.containingFile
//        if(DumbService.isDumb(file.project)) return ResolveResult.EMPTY_ARRAY
//        val definitions = NewDefinitionIndex.getByNameInFileSet(name, file)
//        return definitions.mapNotNull { newcommand ->
//            // Find the command being defined, e.g. \hi in case of \newcommand{\hi}{}
//            // We should resolve to \hi, not to \newcommand, because otherwise the find usages will try to find references to the \hi definition and won't find anything because the references point to the \newcommand
//            val definedCommand = newcommand.definitionCommand()
//            definedCommand?.let { PsiElementResolveResult(definedCommand) }
//        }.toTypedArray()
        val result = resolve() ?: return ResolveResult.EMPTY_ARRAY
        return arrayOf(PsiElementResolveResult(result))
    }

    override fun resolve(): PsiElement? {
        val element = myElement
        if (LatexPsiUtil.isInsideDefinition(element)) {
            return null
        }
        val name = element.name ?: return null
        val file = element.containingFile
        val virtualFile = file.virtualFile ?: return null
        val project = element.project
        if (DumbService.isDumb(project)) {
            // If the project is dumb, we cannot resolve definitions, so return null
            return null
        }
        val defService = LatexDefinitionService.getInstance(project)
        val sourcedDefinition = defService.resolveCommandDef(virtualFile, name) ?: return null
        val defCommand = sourcedDefinition.definitionCommandPointer?.element ?: return null
        return LatexPsiUtil.getDefinedCommandElement(defCommand)
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