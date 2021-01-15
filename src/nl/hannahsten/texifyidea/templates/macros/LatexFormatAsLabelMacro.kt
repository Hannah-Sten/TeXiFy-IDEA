package nl.hannahsten.texifyidea.templates.macros

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.macro.MacroBase
import nl.hannahsten.texifyidea.templates.LatexContext
import nl.hannahsten.texifyidea.util.formatAsLabel

class LatexFormatAsLabelMacro : MacroBase("latexFormatAsLabel", "latexFormatAsLabel(String)") {

    override fun calculateResult(params: Array<out Expression>, context: ExpressionContext?, quick: Boolean): Result? {
        val text = getTextResult(params, context, true) ?: return null
        return TextResult(text.formatAsLabel())
    }

    override fun isAcceptableInContext(context: TemplateContextType?) = context is LatexContext
}