package nl.hannahsten.texifyidea.editor.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelector
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.Function
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.inMathContext

/**
 * This class is also used by https://github.com/xylo/intellij-postfix-templates/blob/idea-232/src/de/endrullis/idea/postfixtemplates/languages/latex/LatexPostfixTemplateProvider.java
 */
class LatexPostfixExpressionSelector(private val mathOnly: Boolean = false, private val textOnly: Boolean = false) : PostfixTemplateExpressionSelector {

    override fun hasExpression(context: PsiElement, copyDocument: Document, newOffset: Int): Boolean {
        return when {
            mathOnly -> context.inMathContext()
            textOnly -> !context.inMathContext()
            else -> true
        }
    }

    override fun getRenderer(): Function<PsiElement, String> {
        return Function(PsiElement::getText)
    }

    override fun getExpressions(context: PsiElement, document: Document, offset: Int): MutableList<PsiElement> {
        return when (context.elementType) {
            LatexTypes.INLINE_MATH_END -> mutableListOf(context.firstParentOfType(LatexInlineMath::class) as PsiElement)
            LatexTypes.CLOSE_BRACE ->
                mutableListOf(
                    context.firstParentOfType(LatexCommands::class)
                        ?: context.firstParentOfType(LatexGroup::class) as PsiElement
                )
            LatexTypes.CLOSE_BRACKET -> mutableListOf(context.firstParentOfType(LatexOptionalParam::class) as PsiElement)
            LatexTypes.DISPLAY_MATH_END -> mutableListOf(context.firstParentOfType(LatexDisplayMath::class) as PsiElement)
            else -> mutableListOf(context)
        }
    }

    fun getTopMostExpression(element: PsiElement): PsiElement {
        return getExpressions(element, element.containingFile.fileDocument, element.textOffset).firstOrNull()
            ?: element
    }
}