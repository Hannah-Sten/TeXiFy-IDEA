package nl.hannahsten.texifyidea.editor.postfix.editable

import com.intellij.codeInsight.template.postfix.templates.editable.PostfixTemplateExpressionCondition
import com.intellij.psi.PsiExpression
import nl.hannahsten.texifyidea.editor.postfix.LatexPostfixExpressionSelector
import nl.hannahsten.texifyidea.util.parser.inMathContext
import org.jdom.Element

sealed class LatexPostfixTemplateExpressionCondition : PostfixTemplateExpressionCondition<PsiExpression> {

    abstract fun expressionSelector(): LatexPostfixExpressionSelector

    companion object {
        fun values() = listOf(
            LatexPostfixTemplateDefaultExpressionCondition(),
            LatexPostfixTemplateTextOnlyExpressionCondition(),
            LatexPostfixTemplateTextOnlyExpressionCondition()
        )

        fun readExternal(condition: Element): LatexPostfixTemplateExpressionCondition {
            val id = condition.getAttributeValue(PostfixTemplateExpressionCondition.ID_ATTR)
            return when (id) {
                LatexPostfixTemplateMathOnlyExpressionCondition.ID -> LatexPostfixTemplateMathOnlyExpressionCondition()
                LatexPostfixTemplateTextOnlyExpressionCondition.ID -> LatexPostfixTemplateTextOnlyExpressionCondition()
                else -> LatexPostfixTemplateDefaultExpressionCondition()
            }
        }
    }
}

class LatexPostfixTemplateDefaultExpressionCondition : LatexPostfixTemplateExpressionCondition() {
    override fun expressionSelector(): LatexPostfixExpressionSelector = LatexPostfixExpressionSelector()

    override fun value(t: PsiExpression): Boolean = true

    override fun getPresentableName(): String = ID

    override fun getId(): String = ID

    companion object {
        const val ID = "default"
    }
}

class LatexPostfixTemplateMathOnlyExpressionCondition : LatexPostfixTemplateExpressionCondition() {

    override fun expressionSelector(): LatexPostfixExpressionSelector = LatexPostfixExpressionSelector(mathOnly = true, textOnly = false)

    override fun value(t: PsiExpression): Boolean = t.inMathContext()

    override fun getPresentableName(): String = ID

    override fun getId(): String = ID

    companion object {
        const val ID = "math only"
    }
}

class LatexPostfixTemplateTextOnlyExpressionCondition : LatexPostfixTemplateExpressionCondition() {

    override fun expressionSelector(): LatexPostfixExpressionSelector = LatexPostfixExpressionSelector(mathOnly = false, textOnly = true)

    override fun value(t: PsiExpression): Boolean = !t.inMathContext()

    override fun getPresentableName(): String = ID

    override fun getId(): String = ID

    companion object {
        const val ID = "text only"
    }
}