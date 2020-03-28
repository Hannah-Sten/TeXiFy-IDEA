package nl.hannahsten.texifyidea.templates.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelector
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplatePsiInfo
import com.intellij.codeInsight.template.postfix.templates.SurroundPostfixTemplateBase
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import nl.hannahsten.texifyidea.editor.surroundwith.LatexPairSurrounder
import nl.hannahsten.texifyidea.psi.LatexPsiHelper


object LatexSurroundWithGroupPostfixTemplate : LatexSurroundPostFixTemplate("group", "{expr}", Pair("{", "}"))

open class LatexSurroundPostFixTemplate(
        name: String,
        description: String,
        private val surrounderPair: Pair<String, String>)
    : SurroundPostfixTemplateBase(
        name, description, LatexPostfixTemplatePsiInfo, LatexPostFixExpressionSelector
) {
    override fun getSurrounder(): Surrounder = LatexPairSurrounder(surrounderPair)
}

object LatexPostfixTemplatePsiInfo : PostfixTemplatePsiInfo() {
    override fun getNegatedExpression(element: PsiElement): PsiElement = element
    override fun createExpression(context: PsiElement, prefix: String, suffix: String): PsiElement =
            LatexPsiHelper(context.project).createFromText(prefix + context.text + suffix)
}

object LatexPostFixExpressionSelector : PostfixTemplateExpressionSelector {
    override fun hasExpression(context: PsiElement, copyDocument: Document, newOffset: Int): Boolean {
        return true
    }

    override fun getRenderer(): Function<PsiElement, String> {
        return Function(PsiElement::getText)
    }

    override fun getExpressions(context: PsiElement, document: Document, offset: Int): MutableList<PsiElement> {
        return mutableListOf(context)
    }

}