package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexGroup
import nl.hannahsten.texifyidea.psi.LatexOptionalParamContent

abstract class LatexKeyValKeyImplMixin(node: ASTNode) : ASTWrapperPsiElement(node) {

    override fun toString(): String {
        // This is ugly, but element.children returns only composite children and other querying methods are recursive
        val result = ArrayList<PsiElement>()
        var psiChild = this.firstChild
        while (psiChild != null) {
            result.add(psiChild)
            psiChild = psiChild.nextSibling
        }
        return result.joinToString(separator = "") {
            when (it) {
                is LatexOptionalParamContent -> it.group?.let { g -> g.groupContent?.text ?: "" } ?: it.text
                is LatexGroup -> it.groupContent?.text ?: ""
                else -> it.text
            }
        }
    }
}