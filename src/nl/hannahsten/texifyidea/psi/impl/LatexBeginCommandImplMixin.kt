package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.reference.LatexEnvironmentDefinitionReference
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.getOptionalParameterMapFromParameters

abstract class LatexBeginCommandImplMixin(node: ASTNode) : LatexBeginCommand, ASTWrapperPsiElement(node) {

    override fun getName(): String {
        return "\\begin"
    }

    override fun getOptionalParameterMap() = getOptionalParameterMapFromParameters(this.parameterList)

    override fun getReference(): PsiReference? {
        return LatexEnvironmentDefinitionReference(firstParentOfType<LatexEnvironment>()?:return null)
    }

}