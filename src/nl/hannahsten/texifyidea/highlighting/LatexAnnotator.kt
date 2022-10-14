package nl.hannahsten.texifyidea.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.isContext
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.requiredParameters

/**
 * Provide syntax highlighting for composite elements.
 *
 * @author Hannah Schellekens
 */
open class LatexAnnotator : Annotator {

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        // Math display
        if (psiElement is LatexInlineMath) {
            annotateInlineMath(psiElement, annotationHolder)
        }
        else if (psiElement is LatexDisplayMath ||
            (psiElement is LatexEnvironment && psiElement.isContext(Environment.Context.MATH))
        ) {
            annotateDisplayMath(psiElement, annotationHolder)

            // Begin/End commands
            if (psiElement is LatexEnvironment) {
                annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, "")
                    .range(TextRange.from(psiElement.beginCommand.textOffset, 6))
                    .textAttributes(LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY)
                    .create()

                annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, "")
                    .range(TextRange.from(psiElement.endCommand?.textOffset ?: psiElement.textOffset, 4))
                    .textAttributes(LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY)
                    .create()
            }
        }
        // Optional parameters
        else if (psiElement is LatexOptionalParam) {
            annotateOptionalParameters(psiElement, annotationHolder)
        }
        // Commands
        else if (psiElement is LatexCommands) {
            annotateCommands(psiElement, annotationHolder)
        }
    }

    /**
     * Annotates an inline math element and its children.
     *
     * All elements will be coloured accoding to [LatexSyntaxHighlighter.INLINE_MATH] and
     * all commands that are contained in the math environment get styled with
     * [LatexSyntaxHighlighter.COMMAND_MATH_INLINE].
     */
    private fun annotateInlineMath(
        inlineMathElement: LatexInlineMath,
        annotationHolder: AnnotationHolder
    ) {
        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(inlineMathElement)
            .textAttributes(LatexSyntaxHighlighter.INLINE_MATH)
            .create()

        annotateMathCommands(
            inlineMathElement.childrenOfType(LatexCommands::class), annotationHolder,
            LatexSyntaxHighlighter.COMMAND_MATH_INLINE
        )
    }

    /**
     * Annotates a display math element and its children.
     *
     * All elements will be coloured accoding to [LatexSyntaxHighlighter.DISPLAY_MATH] and
     * all commands that are contained in the math environment get styled with
     * [LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY].
     */
    private fun annotateDisplayMath(
        displayMathElement: PsiElement,
        annotationHolder: AnnotationHolder
    ) {
        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(displayMathElement)
            .textAttributes(LatexSyntaxHighlighter.DISPLAY_MATH)
            .create()

        annotateMathCommands(
            displayMathElement.childrenOfType(LatexCommands::class), annotationHolder,
            LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY
        )
    }

    /**
     * Annotates all command tokens of the commands that are included in the `elements`.
     *
     * @param elements
     *              All elements to handle. Only elements that are [LatexCommands] are considered.
     * @param highlighter
     *              The attributes to apply to all command tokens.
     */
    private fun annotateMathCommands(
        elements: Collection<PsiElement>,
        annotationHolder: AnnotationHolder,
        highlighter: TextAttributesKey
    ) {
        for (element in elements) {
            if (element !is LatexCommands) {
                continue
            }

            val token = element.commandToken

            annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(token)
                .textAttributes(highlighter)
                .create()

            if (element.name == "\\text" || element.name == "\\intertext") {
                annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element.requiredParameters().firstOrNull() ?: continue)
                    .textAttributes(LatexSyntaxHighlighter.MATH_NESTED_TEXT)
                    .create()
            }
        }
    }

    /**
     * Annotates the given optional parameters of commands.
     */
    private fun annotateOptionalParameters(
        optionalParamElement: LatexOptionalParam,
        annotationHolder: AnnotationHolder
    ) {
        for (
            element in optionalParamElement.optionalParamContentList
        ) {
            if (element !is LatexOptionalParamContent) {
                continue
            }

            val toStyle = element.parameterText ?: continue

            annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(toStyle)
                .textAttributes(LatexSyntaxHighlighter.OPTIONAL_PARAM)
                .create()
        }
    }

    /**
     * Annotates the given required parameters of commands.
     */
    private fun annotateCommands(command: LatexCommands, annotationHolder: AnnotationHolder) {
        annotateStyle(command, annotationHolder)

        // Label references.
        val style = when (command.name) {
            in CommandMagic.labelReferenceWithoutCustomCommands -> {
                LatexSyntaxHighlighter.LABEL_REFERENCE
            }
            // Label definitions.
            in getLabelDefinitionCommands() -> {
                LatexSyntaxHighlighter.LABEL_DEFINITION
            }
            // Bibliography references (citations).
            in CommandMagic.bibliographyReference -> {
                LatexSyntaxHighlighter.BIBLIOGRAPHY_REFERENCE
            }
            // Label definitions.
            in CommandMagic.bibliographyItems -> {
                LatexSyntaxHighlighter.BIBLIOGRAPHY_DEFINITION
            }
            else -> return
        }

        command.requiredParameters().firstOrNull()?.let {
            annotationHolder.annotateRequiredParameter(it, style)
        }
    }

    /**
     * Annotates the command according to its font style, i.e. \textbf{} gets annotated with the `STYLE_BOLD` style.
     */
    private fun annotateStyle(command: LatexCommands, annotationHolder: AnnotationHolder) {
        val style = when (command.name) {
            "\\textbf" -> LatexSyntaxHighlighter.STYLE_BOLD
            "\\textit" -> LatexSyntaxHighlighter.STYLE_ITALIC
            "\\underline" -> LatexSyntaxHighlighter.STYLE_UNDERLINE
            "\\sout" -> LatexSyntaxHighlighter.STYLE_STRIKETHROUGH
            "\\textsc" -> LatexSyntaxHighlighter.STYLE_SMALL_CAPITALS
            "\\overline" -> LatexSyntaxHighlighter.STYLE_OVERLINE
            "\\texttt" -> LatexSyntaxHighlighter.STYLE_TYPEWRITER
            "\\textsl" -> LatexSyntaxHighlighter.STYLE_SLANTED
            else -> return
        }

        command.requiredParameters().firstOrNull()?.let {
            annotationHolder.annotateRequiredParameter(it, style)
        }
    }

    /**
     * Annotates the contents of the given parameter with the given style.
     */
    private fun AnnotationHolder.annotateRequiredParameter(parameter: LatexRequiredParam, style: TextAttributesKey) {
        val content = parameter.firstChildOfType(LatexContent::class) ?: return
        this.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(content)
            .textAttributes(style)
            .create()
    }
}
