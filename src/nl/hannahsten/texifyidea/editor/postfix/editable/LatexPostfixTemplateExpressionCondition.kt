package nl.hannahsten.texifyidea.editor.postfix.editable

import com.intellij.codeInsight.template.postfix.templates.editable.PostfixTemplateExpressionCondition
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.editor.postfix.LatexPostfixExpressionSelector
import nl.hannahsten.texifyidea.util.parser.inMathContext
import org.jdom.Element

sealed class LatexPostfixTemplateExpressionCondition : PostfixTemplateExpressionCondition<PsiElement> {

    abstract fun expressionSelector(): LatexPostfixExpressionSelector

    companion object {
        fun selectableValues() = setOf(
            LatexPostfixTemplateTextOnlyExpressionCondition(),
            LatexPostfixTemplateMathOnlyExpressionCondition()
        )

        fun readExternal(condition: Element): LatexPostfixTemplateExpressionCondition {
            val id = condition.getAttributeValue(PostfixTemplateExpressionCondition.ID_ATTR)
            return when (id) {
                LatexPostfixTemplateMathOnlyExpressionCondition.ID -> LatexPostfixTemplateMathOnlyExpressionCondition()
                LatexPostfixTemplateTextOnlyExpressionCondition.ID -> LatexPostfixTemplateTextOnlyExpressionCondition()
                else -> throw IllegalStateException("Invalid condition $condition")
            }
        }
    }
}

class LatexPostfixTemplateMathOnlyExpressionCondition : LatexPostfixTemplateExpressionCondition() {

    override fun expressionSelector(): LatexPostfixExpressionSelector = LatexPostfixExpressionSelector(mathOnly = true, textOnly = false)

    override fun value(t: PsiElement): Boolean = t.inMathContext()

    override fun getPresentableName(): String = ID

    override fun getId(): String = ID

    companion object {
        const val ID = "math only"
    }
}

class LatexPostfixTemplateTextOnlyExpressionCondition : LatexPostfixTemplateExpressionCondition() {

    override fun expressionSelector(): LatexPostfixExpressionSelector = LatexPostfixExpressionSelector(mathOnly = false, textOnly = true)

    override fun value(t: PsiElement): Boolean = !t.inMathContext()

    override fun getPresentableName(): String = ID

    override fun getId(): String = ID

    companion object {
        const val ID = "text only"
    }
}