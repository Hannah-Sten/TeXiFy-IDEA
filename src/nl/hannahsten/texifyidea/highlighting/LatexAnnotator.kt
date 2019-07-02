package nl.hannahsten.texifyidea.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.definitionsAndRedefinitionsInFileSet

/**
 * @author Hannah Schellekens
 */
open class LatexAnnotator : Annotator {

    companion object {

        /**
         * The maximum amount of times the cache may be used before doing another lookup.
         */
        private const val MAX_CACHE_COUNT = 40
    }

    // Cache to prevent many PsiFile#isUsed and PsiFile#definitions() lookups.
    private var definitionCache: Collection<LatexCommands>? = null
    private var definitionCacheFile: PsiFile? = null
    private var definitionCacheCount: Int = 0

    /**
     * Looks up all the definitions in the file set.
     *
     * It does cache all the definitions for [MAX_CACHE_COUNT] lookups.
     * See also members [definitionCache], [definitionCacheFile], and [definitionCacheCount]
     */
    private fun PsiFile.definitionCache(): Collection<LatexCommands> {
        // Initialise.
        if (definitionCache == null) {
            definitionCache = definitionsAndRedefinitionsInFileSet().filter { it.isEnvironmentDefinition() }
            definitionCacheFile = this
            definitionCacheCount = 0
            return definitionCache!!
        }

        // Check if the file is the same.
        if (definitionCacheFile != this) {
            definitionCache = definitionsAndRedefinitionsInFileSet().filter { it.isEnvironmentDefinition() }
            definitionCacheCount = 0
            definitionCacheFile = this
        }

        // Re-evaluate after the count has been reached times.
        if (++definitionCacheCount > MAX_CACHE_COUNT) {
            definitionCache = definitionsAndRedefinitionsInFileSet().filter { it.isEnvironmentDefinition() }
            definitionCacheCount = 0
        }

        return definitionCache!!
    }

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        // Comments
        if (psiElement is PsiComment) {
            annotateComment(psiElement, annotationHolder)
        }
        else if (psiElement.inDirectEnvironmentContext(Environment.Context.COMMENT)) {
            annotateComment(psiElement, annotationHolder)
        }
        // Math display
        else if (psiElement is LatexInlineMath) {
            annotateInlineMath(psiElement, annotationHolder)
        }
        else if (psiElement is LatexDisplayMath ||
                (psiElement is LatexEnvironment && psiElement.isContext(Environment.Context.MATH))) {
            annotateDisplayMath(psiElement, annotationHolder)

            // Begin/End commands
            if (psiElement is LatexEnvironment) {
                val ann1 = annotationHolder.createInfoAnnotation(TextRange.from(psiElement.beginCommand.textOffset, 6), null)
                ann1.textAttributes = LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY
                val ann2 = annotationHolder.createInfoAnnotation(TextRange.from(psiElement.endCommand.textOffset, 4), null)
                ann2.textAttributes = LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY
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
     * Annotates the given comment.
     */
    private fun annotateComment(comment: PsiElement, annotationHolder: AnnotationHolder) {
        val file = comment.containingFile
        val hasDefinition = file.definitionCache().any { it.requiredParameter(0) == "comment" }
        if (hasDefinition) {
            return
        }

        val annotation = annotationHolder.createInfoAnnotation(comment, null)
        annotation.textAttributes = if (comment.isMagicComment()) {
            LatexSyntaxHighlighter.MAGIC_COMMENT
        }
        else LatexSyntaxHighlighter.COMMENT
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

            val noMathContent = element.noMathContent
            val toStyle = noMathContent.normalText ?: continue
            val annotation = annotationHolder.createInfoAnnotation(toStyle, null)
            annotation.textAttributes = LatexSyntaxHighlighter.OPTIONAL_PARAM
        }
    }

    /**
     * Annotates the given required parameters of commands.
     */
    private fun annotateCommands(command: LatexCommands, annotationHolder: AnnotationHolder) {
        annotateStyle(command, annotationHolder)

        // Label references.
        val style = if (command.name in Magic.Command.labelReference) {
            LatexSyntaxHighlighter.LABEL_REFERENCE
        }
        // Label definitions.
        else if (command.name in Magic.Command.labelDefinition) {
            LatexSyntaxHighlighter.LABEL_DEFINITION
        }
        // Bibliography references (citations).
        else if (command.name in Magic.Command.bibliographyReference) {
            LatexSyntaxHighlighter.BIBLIOGRAPHY_REFERENCE
        }
        // Label definitions.
        else if (command.name in Magic.Command.bibliographyItems) {
            LatexSyntaxHighlighter.BIBLIOGRAPHY_DEFINITION
        }
        else return

        command.requiredParameters().firstOrNull()?.let {
            annotationHolder.annotateRequiredParameter(it, style!!)
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
        val annotation = createInfoAnnotation(content, null)
        annotation.textAttributes = style
    }
}
