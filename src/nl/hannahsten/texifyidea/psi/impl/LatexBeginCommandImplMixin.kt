package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.util.psi.getOptionalParameterMapFromParameters

abstract class LatexBeginCommandImplMixin(node: ASTNode) : LatexBeginCommand, ASTWrapperPsiElement(node) {

    override fun getOptionalParameterMap() = getOptionalParameterMapFromParameters(this.parameterList)
}