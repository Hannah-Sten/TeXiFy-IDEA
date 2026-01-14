package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import nl.hannahsten.texifyidea.psi.LatexKeyValValue

abstract class LatexKeyValValueImplMixin(node: ASTNode) : LatexKeyValValue, ASTWrapperPsiElement(node) {

    override fun toString(): String = keyValContentList.joinToString(separator = "") {
        when {
            it.parameterText != null -> it.parameterText!!.text
            it.parameterGroup != null -> it.parameterGroup!!.parameterGroupText!!.text
            else -> ""
        }
    }
}