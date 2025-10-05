package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import nl.hannahsten.texifyidea.psi.LatexParameter

abstract class LatexParameterImplMixin(node: ASTNode) : ASTWrapperPsiElement(node), LatexParameter {

    override fun toString(): String {
        return "Parameter"
    }

    override fun isValidHost(): Boolean {
        return true
    }

    override fun updateText(text: String): PsiLanguageInjectionHost {
        return ElementManipulators.handleContentChange(this, text)
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<LatexParameter> {
        return LiteralTextEscaper.createSimple(this)
    }
}