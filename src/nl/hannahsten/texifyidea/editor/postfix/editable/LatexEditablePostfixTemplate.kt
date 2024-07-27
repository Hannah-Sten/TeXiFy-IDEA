package nl.hannahsten.texifyidea.editor.postfix.editable

import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.postfix.templates.editable.EditablePostfixTemplateWithMultipleExpressions
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.editor.postfix.LatexPostFixTemplateProvider
import nl.hannahsten.texifyidea.editor.postfix.LatexPostfixExpressionSelector

class LatexEditablePostfixTemplate(templateId: String, templateName: String, template: TemplateImpl, conditions: Set<LatexPostfixTemplateExpressionCondition>, provider: LatexPostFixTemplateProvider) :
    EditablePostfixTemplateWithMultipleExpressions<LatexPostfixTemplateExpressionCondition>(templateId, templateName, template, "", conditions, true, provider) {

    constructor(templateId: String, templateName: String, templateText: String, conditions: Set<LatexPostfixTemplateExpressionCondition>, provider: LatexPostFixTemplateProvider) :
        this(templateId, templateName, createTemplateFromText(templateText), conditions, provider)

    override fun getExpressions(context: PsiElement, document: Document, offset: Int): MutableList<PsiElement> {
        return if (myExpressionConditions.isEmpty()) {
            LatexPostfixExpressionSelector().getExpressions(context, document, offset)
        }
        else myExpressionConditions.flatMap { condition ->
            condition.expressionSelector().getExpressions(context, document, offset).filter { condition.value(it) }
        }.toSet().toMutableList()
    }

    override fun getTopmostExpression(element: PsiElement): PsiElement {
        return getExpressions(element, element.containingFile.fileDocument, element.textOffset)
            .minByOrNull { it.textOffset }!!
    }

    override fun isBuiltin(): Boolean {
        return false
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