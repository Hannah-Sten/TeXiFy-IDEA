package nl.hannahsten.texifyidea.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.SourcedDefinition.DefinitionSource
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.commands.LatexGenericMathCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand.*
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.*
import nl.hannahsten.texifyidea.util.shrink

/**
 * Provide syntax highlighting for composite elements.
 *
 * @author Hannah Schellekens
 */
open class LatexAnnotator : Annotator {

    object Cache {
        internal val userDataKeyDefBundle = Key.create<DefinitionBundle>("LatexAnnotator.defBundle")
    }

    private fun getDefBundle(annotationHolder: AnnotationHolder): DefinitionBundle {
        val session = annotationHolder.currentAnnotationSession
        session.getUserData(Cache.userDataKeyDefBundle)?.let { return it }
        val file = session.file
        val defBundle = LatexDefinitionService.getInstance(file.project).getDefBundlesMerged(file)
        session.putUserData(Cache.userDataKeyDefBundle, defBundle)
        return defBundle
    }

    override fun annotate(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        val defBundle = getDefBundle(annotationHolder)
        val context = LatexPsiUtil.resolveContextUpward(psiElement, defBundle)
        when {
            psiElement is LatexInlineMath -> {
                annotateInlineMath(psiElement, annotationHolder)
            }

            psiElement is LatexDisplayMath ||
                (psiElement is LatexEnvironment && LatexPsiUtil.isContextIntroduced(psiElement, defBundle, LatexContexts.Math)) -> {
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
            psiElement is LatexOptionalKeyValPair -> {
                annotateKeyValuePair(psiElement, annotationHolder)
            }
            // Optional parameters.
            psiElement is LatexOptionalParam -> {
                annotateOptionalParameters(psiElement, annotationHolder)
            }
            // Commands.
            psiElement is LatexCommands -> {
                if (context.contains(LatexContexts.InlineMath)) {
                    annotateMathCommands(psiElement, annotationHolder, LatexSyntaxHighlighter.COMMAND_MATH_INLINE)
                }
                else if (context.contains(LatexContexts.Math)) {
                    annotateMathCommands(psiElement, annotationHolder, LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY)
                }
                annotateCommands(psiElement, annotationHolder, defBundle)
            }

            psiElement.elementType == LatexTypes.LEFT || psiElement.elementType == LatexTypes.RIGHT -> {
                annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, "")
                    .range(psiElement)
                    .textAttributes(LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY)
                    .create()
            }

            psiElement is PsiComment || context.contains(LatexContexts.Comment) -> {
                annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(psiElement.textRange)
                    .textAttributes(LatexSyntaxHighlighter.COMMENT)
                    .create()
            }
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

//        annotateMathCommands(
//            displayMathElement.collectSubtreeTyped<LatexCommands>(), annotationHolder,
//            LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY
//        )
    }

    /**
     * Annotates all command tokens of the commands that are included in the `elements`.
     *
     * @param element the command
     * @param highlighter
     *              The attributes to apply to all command tokens.
     */
    private fun annotateMathCommands(
        element: LatexCommands,
        annotationHolder: AnnotationHolder,
        highlighter: TextAttributesKey
    ) {
        val token = element.commandToken

        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(token)
            .textAttributes(highlighter)
            .create()

        if (element.name == TEXT.cmd || element.name == INTERTEXT.name) {
            // Avoid creating an Annotation without calling the create() method
            val range = element.firstRequiredParameter() ?: return
            annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(range)
                .textAttributes(LatexSyntaxHighlighter.MATH_NESTED_TEXT)
                .create()
        }
    }

    private fun annotateKeyValuePair(element: LatexOptionalKeyValPair, annotationHolder: AnnotationHolder) {
        val value = element.keyValValue ?: return

        annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(TextRange(element.optionalKeyValKey.endOffset, value.startOffset))
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
        element in optionalParamElement.optionalKeyValPairList
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

    private fun getStyleFromContextSignature(intro: LatexContextIntro): TextAttributesKey? {
        if (intro is LatexContextIntro.Assign) {
            for (ctx in intro.contexts) {
                when (ctx) {
                    LatexContexts.LabelReference -> return LatexSyntaxHighlighter.LABEL_REFERENCE
                    LatexContexts.LabelDefinition -> return LatexSyntaxHighlighter.LABEL_DEFINITION
                    LatexContexts.BibReference -> return LatexSyntaxHighlighter.BIBLIOGRAPHY_REFERENCE
                    LatexContexts.BibKey -> return LatexSyntaxHighlighter.BIBLIOGRAPHY_DEFINITION
                }
            }
        }
        return null
    }

    /**
     * Annotates the given required parameters of commands.
     */
    private fun annotateCommands(command: LatexCommands, annotationHolder: AnnotationHolder, defBundle: DefinitionBundle) {
        annotateStyle(command, annotationHolder)

        // Make user-defined commands highlighting customizable
        val name = command.nameWithoutSlash ?: return
        val def = defBundle.findDefinition(name) ?: return
        val semantics = def.entity as? LSemanticCommand ?: return

        if (def.source == DefinitionSource.UserDefined) {
            annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(LatexSyntaxHighlighter.USER_DEFINED_COMMAND_KEY)
                .range(command.commandToken)
                .create()
        }

        LatexPsiUtil.processArgumentsWithNonNullSemantics(command, semantics) { param, arg ->
            val intro = arg.contextSignature
            getStyleFromContextSignature(intro)?.let { style ->
                annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(param.textRange.shrink(1))
                    .textAttributes(style)
                    .create()
            }
        }
    }

    /**
     * Annotates the command according to its font style, i.e. \textbf{} gets annotated with the `STYLE_BOLD` style.
     */
    private fun annotateStyle(command: LatexCommands, annotationHolder: AnnotationHolder) {
        val style = when (command.name) {
            TEXTBF.cmd -> LatexSyntaxHighlighter.STYLE_BOLD
            TEXTIT.cmd -> LatexSyntaxHighlighter.STYLE_ITALIC
            LatexGenericRegularCommand.UNDERLINE.cmd -> LatexSyntaxHighlighter.STYLE_UNDERLINE
            SOUT.cmd -> LatexSyntaxHighlighter.STYLE_STRIKETHROUGH
            TEXTSC.cmd -> LatexSyntaxHighlighter.STYLE_SMALL_CAPITALS
            OVERLINE.cmd -> LatexSyntaxHighlighter.STYLE_OVERLINE
            TEXTTT.cmd -> LatexSyntaxHighlighter.STYLE_TYPEWRITER
            TEXTSL.cmd -> LatexSyntaxHighlighter.STYLE_SLANTED
            else -> return
        }

        command.firstRequiredParameter()?.let { param ->
            annotationHolder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(param.textRange.shrink(1))
                .textAttributes(style)
                .create()
        }
    }
}
