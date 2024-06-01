package nl.hannahsten.texifyidea.editor.postfix.editable

import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.postfix.templates.editable.EditablePostfixTemplateWithMultipleExpressions
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.editor.postfix.LatexPostFixTemplateProvider

class LatexEditablePostfixTemplate(templateId: String, templateName: String, template: TemplateImpl, private val condition: LatexPostfixTemplateExpressionCondition, provider: LatexPostFixTemplateProvider)
    : EditablePostfixTemplateWithMultipleExpressions<LatexPostfixTemplateExpressionCondition>(templateId, templateName, template, "", setOf(condition), true, provider) {

        constructor(templateId: String, templateName: String, templateText: String, condition: LatexPostfixTemplateExpressionCondition, provider: LatexPostFixTemplateProvider)
            : this(templateId, templateName, createTemplateFromText(templateText), condition, provider)

    override fun getExpressions(context: PsiElement, document: Document, offset: Int): MutableList<PsiElement> {
        return condition.expressionSelector().getExpressions(context, document, offset)
    }

    override fun getTopmostExpression(element: PsiElement): PsiElement {
        return condition.expressionSelector().getTopMostExpression(element)
    }

    companion object {
        private fun createTemplateFromText(templateText: String): TemplateImpl {
            return TemplateImpl("fakeKey", templateText, "").apply {
                isToReformat = true
                parseSegments()
            }
        }
    }
}