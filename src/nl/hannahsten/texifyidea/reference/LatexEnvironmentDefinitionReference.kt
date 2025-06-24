package nl.hannahsten.texifyidea.reference

import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import nl.hannahsten.texifyidea.index.NewDefinitionIndex
import nl.hannahsten.texifyidea.psi.LatexEnvIdentifier
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped

class LatexEnvironmentDefinitionReference(element: LatexEnvIdentifier) : PsiReferenceBase<LatexEnvIdentifier>(element), PsiPolyVariantReference {

    init {
        rangeInElement = ElementManipulators.getValueTextRange(element)
    }

    override fun resolve(): PsiElement? {
        return multiResolve(false).firstOrNull()?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val name = element.name ?: return ResolveResult.EMPTY_ARRAY
        return NewDefinitionIndex.getByName(name, element.project).mapNotNull {
            val firstParam = it.parameterList.firstOrNull() ?: return@mapNotNull null
            val paramText = firstParam.findFirstChildTyped<LatexParameterText>() ?: return@mapNotNull null
            PsiElementResolveResult(paramText)
        }.toTypedArray()
    }
}