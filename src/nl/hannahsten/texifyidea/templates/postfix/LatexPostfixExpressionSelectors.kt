package nl.hannahsten.texifyidea.templates.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelector
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.inMathContext

class LatexPostfixExpressionSelector(private val mathOnly: Boolean = false, private val textOnly: Boolean = false) : PostfixTemplateExpressionSelector {
    override fun hasExpression(context: PsiElement, copyDocument: Document, newOffset: Int): Boolean {
        return when {
            mathOnly -> context.inMathContext()
            textOnly -> !context.inMathContext()
            else -> (context.parent is LatexNormalText || context is LatexNormalText)
        }
    }

    override fun getRenderer(): Function<PsiElement, String> {
        return Function(PsiElement::getText)
    }

    override fun getExpressions(context: PsiElement, document: Document, offset: Int): MutableList<PsiElement> {
        return mutableListOf(context)
    }
}