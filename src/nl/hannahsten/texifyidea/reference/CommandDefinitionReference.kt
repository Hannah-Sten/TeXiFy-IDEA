package nl.hannahsten.texifyidea.reference

import com.intellij.psi.*
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.projectSearchScope

/**
 * @author Abby Berkers
 */
class CommandDefinitionReference(element: LatexCommands) : PsiReferenceBase<LatexCommands>(element), PsiPolyVariantReference {
    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = element.project
        return LatexDefinitionIndex.getCommandsByName(element.name ?: return emptyArray(), project, project.projectSearchScope)
                .map { PsiElementResolveResult(it.commandToken) }
                .toArray(emptyArray())
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
//        return multiResolve(false).any { it.element == element }
        return true
    }
}