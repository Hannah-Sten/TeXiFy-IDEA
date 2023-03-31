package nl.hannahsten.texifyidea.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic

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
        // Key value pairs. Match on the common interface so we catch LatexKeyValPair and LatexStrictKeyValPair.
        else if (psiElement is LatexKeyValuePair) {
            annotateKeyValuePair(psiElement, annotationHolder)
        }
        // Optional parameters.
        else if (psiElement is LatexOptionalParam) {
            annotateOptionalParameters(psiElement, annotationHolder)
        }
        // Commands.
        else if (psiElement is LatexCommands) {
            annotateCommands(psiElement, annotationHolder)
        }
        else if (psiElement.isComment()) {
            annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(psiElement.textRange)
                .textAttributes(LatexSyntaxHighlighter.COMMENT)
                .create()
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
                // Avoid creating an Annotation without calling the create() method
                val range = element.requiredParameters().firstOrNull() ?: continue
                annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(range)
                    .textAttributes(LatexSyntaxHighlighter.MATH_NESTED_TEXT)
                    .create()
            }
        }
    }

    private fun annotateKeyValuePair(element: LatexKeyValuePair, annotationHolder: AnnotationHolder) {
        element.keyValValue ?: return

        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(TextRange(element.keyValKey.endOffset, element.keyValValue!!.startOffset))
            .textAttributes(LatexSyntaxHighlighter.SEPARATOR_EQUALS)
            .create()
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

        // Make user-defined commands highlighting customizable
        val allUserCommands = LatexDefinitionIndex.getItems(command.project)
            .filter { it.isCommandDefinition() }
            .map { it.definedCommandName() }
        if (command.name in allUserCommands) {
            annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(LatexSyntaxHighlighter.USER_DEFINED_COMMAND_KEY)
                .range(command.commandToken)
                .create()
        }

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
    @Suppress("USELESS_CAST")
    private fun AnnotationHolder.annotateRequiredParameter(parameter: LatexRequiredParam, style: TextAttributesKey) {
        val firstContentChild = parameter.firstChildOfType(LatexContent::class)
        val firstParamChild = parameter.firstChildOfType(LatexRequiredParamContent::class)

        if (firstContentChild != null) {
            this.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(firstContentChild)
                .textAttributes(style)
                .create()
        }
        else if (firstParamChild != null) {
            parameter.childrenOfType(LeafPsiElement::class)
                .filter {
                    it.elementType == LatexTypes.NORMAL_TEXT_WORD
                }
                .map {
                    it as PsiElement
                }
                .forEach {
                    this.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(it)
                        .textAttributes(style)
                        .create()
                }
        }
    }
}
