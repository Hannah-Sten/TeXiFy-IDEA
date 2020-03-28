package nl.hannahsten.texifyidea.templates.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelector
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplatePsiInfo
import com.intellij.codeInsight.template.postfix.templates.SurroundPostfixTemplateBase
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import nl.hannahsten.texifyidea.psi.LatexPsiHelper

internal object LatexSurroundWithGroupPostfixTemplate : SurroundPostfixTemplateBase(
        "group", "{expr}", object : PostfixTemplatePsiInfo() {
    override fun getNegatedExpression(element: PsiElement): PsiElement {
        return element
    }

    override fun createExpression(context: PsiElement, prefix: String, suffix: String): PsiElement {
        return LatexPsiHelper(context.project).createFromText(prefix + context.text + suffix)
    }
}, object : PostfixTemplateExpressionSelector {
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
) {

    override fun getSurrounder(): Surrounder {
        return object : Surrounder {
            override fun isApplicable(elements: Array<out PsiElement>): Boolean {
                return true
            }

            override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
                return TextRange(
                        elements.minBy { it.textOffset }?.textOffset
                                ?: return null,
                        elements.maxBy { it.textRange.endOffset }?.textRange?.endOffset
                                ?: return null
                )
            }

            override fun getTemplateDescription(): String {
                return description
            }

        }
    }
}