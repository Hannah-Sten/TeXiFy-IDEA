package nl.hannahsten.texifyidea.reference

import com.intellij.psi.*
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.extractLabelName
import nl.hannahsten.texifyidea.util.findLatexLabelPsiElementsInFileAsSequence

/**
 * The difference with [LatexLabelReference] is that this reference works on normal text, i.e. the actual label parameters.
 * This means that the parameter of a \ref command will resolve to the parameter of the \label command.
 *
 * This allows us to implement find usages as well
 */
class LatexLabelParameterReference(element: LatexNormalText) : PsiReferenceBase<LatexNormalText>(element), PsiPolyVariantReference {

    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return multiResolve(false).any { it.element == element }
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        // Find the label definition
        return myElement.containingFile.findLatexLabelPsiElementsInFileAsSequence()
                .filter { it.extractLabelName() == myElement.name }
                .map { PsiElementResolveResult(it) }
                .toList()
                .toArray(emptyArray())
    }
}