package nl.rubensten.texifyidea.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.lang.Environment
import nl.rubensten.texifyidea.psi.*
import nl.rubensten.texifyidea.util.childrenOfType
import nl.rubensten.texifyidea.util.inDirectEnvironmentMatching

/**
 * @author Ruben Schellekens
 */
open class LatexAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        // Math display
        if (psiElement is LatexInlineMath) {
            annotateInlineMath(psiElement, annotationHolder)
        }
        else if (psiElement is LatexDisplayMath) {
            annotateDisplayMath(psiElement, annotationHolder)
        }
        else if (psiElement.inDirectEnvironmentMatching {
            Environment.fromPsi(it)?.context == Environment.Context.MATH
        }) {
            annotateDisplayMath(psiElement, annotationHolder)
        }
        // Optional parameters
        else if (psiElement is LatexOptionalParam) {
            annotateOptionalParameters(psiElement, annotationHolder)
        }
    }

    /**
     * Annotates an inline math element and its children.
     *
     * All elements will  be coloured accoding to [LatexSyntaxHighlighter.INLINE_MATH] and
     * all commands that are contained in the math environment get styled with
     * [LatexSyntaxHighlighter.COMMAND_MATH_INLINE].
     */
    private fun annotateInlineMath(inlineMathElement: LatexInlineMath,
                                   annotationHolder: AnnotationHolder) {
        val annotation = annotationHolder.createInfoAnnotation(inlineMathElement, null)
        annotation.textAttributes = LatexSyntaxHighlighter.INLINE_MATH

        annotateMathCommands(LatexPsiUtil.getAllChildren(inlineMathElement), annotationHolder,
                LatexSyntaxHighlighter.COMMAND_MATH_INLINE)
    }

    /**
     * Annotates a display math element and its children.
     *
     * All elements will be coloured accoding to [LatexSyntaxHighlighter.DISPLAY_MATH] and
     * all commands that are contained in the math environment get styled with
     * [LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY].
     */
    private fun annotateDisplayMath(displayMathElement: PsiElement,
                                    annotationHolder: AnnotationHolder) {
        val annotation = annotationHolder.createInfoAnnotation(displayMathElement, null)
        annotation.textAttributes = LatexSyntaxHighlighter.DISPLAY_MATH

        annotateMathCommands(displayMathElement.childrenOfType(LatexCommands::class), annotationHolder,
                LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY)
    }

    /**
     * Annotates all command tokens of the comands that are included in the `elements`.
     *
     * @param elements
     *              All elements to handle. Only elements that are [LatexCommands] are considered.
     * @param highlighter
     *              The attributes to apply to all command tokens.
     */
    private fun annotateMathCommands(elements: Collection<PsiElement>,
                                     annotationHolder: AnnotationHolder,
                                     highlighter: TextAttributesKey) {
        for (element in elements) {
            if (element !is LatexCommands) {
                continue
            }

            val token = element.commandToken
            val annotation = annotationHolder.createInfoAnnotation(token, null)
            annotation.textAttributes = highlighter
        }
    }

    /**
     * Annotates the given optional parameters of commands.
     */
    private fun annotateOptionalParameters(optionalParamElement: LatexOptionalParam,
                                           annotationHolder: AnnotationHolder) {
        for (element in optionalParamElement.openGroup.contentList) {
            if (element !is LatexContent) {
                continue
            }

            val noMathContent = element.noMathContent ?: continue
            val toStyle = noMathContent.normalText ?: continue
            val annotation = annotationHolder.createInfoAnnotation(toStyle, null)
            annotation.textAttributes = LatexSyntaxHighlighter.OPTIONAL_PARAM
        }
    }
}
