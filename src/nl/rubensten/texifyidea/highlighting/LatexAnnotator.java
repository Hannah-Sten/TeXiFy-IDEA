package nl.rubensten.texifyidea.highlighting;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import nl.rubensten.texifyidea.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        // Math display
        if (psiElement instanceof LatexInlineMath) {
            annotateInlineMath((LatexInlineMath)psiElement, annotationHolder);
        }
        else if (psiElement instanceof LatexDisplayMath) {
            annotateDisplayMath((LatexDisplayMath)psiElement, annotationHolder);
        }
        // Optional parameters
        else if (psiElement instanceof LatexOptionalParam) {
            annotateOptionalParameters((LatexOptionalParam)psiElement, annotationHolder);
        }
        // Comment
        else if (psiElement instanceof PsiComment) {
            annotateComment((PsiComment)psiElement, annotationHolder);
        }
    }

    /**
     * Annotates an inline math element and its children.
     * <p>
     * All elements will  be coloured accoding to {@link LatexSyntaxHighlighter#INLINE_MATH} and all
     * commands that are contained in the math environment get styled with {@link
     * LatexSyntaxHighlighter#COMMAND_MATH_INLINE}.
     */
    private void annotateInlineMath(@NotNull LatexInlineMath inlineMathElement,
                                    @NotNull AnnotationHolder annotationHolder) {
        Annotation annotation = annotationHolder.createInfoAnnotation(inlineMathElement, null);
        annotation.setTextAttributes(LatexSyntaxHighlighter.INLINE_MATH);

        annotateMathCommands(LatexPsiUtil.getAllChildren(inlineMathElement), annotationHolder,
                LatexSyntaxHighlighter.COMMAND_MATH_INLINE);
    }

    /**
     * Annotates a display math element and its children.
     * <p>
     * All elements will  be coloured accoding to {@link LatexSyntaxHighlighter#DISPLAY_MATH} and
     * all commands that are contained in the math environment get styled with {@link
     * LatexSyntaxHighlighter#COMMAND_MATH_DISPLAY}.
     */
    private void annotateDisplayMath(@NotNull LatexDisplayMath displayMathElement,
                                     @NotNull AnnotationHolder annotationHolder) {
        Annotation annotation = annotationHolder.createInfoAnnotation(displayMathElement, null);
        annotation.setTextAttributes(LatexSyntaxHighlighter.DISPLAY_MATH);

        List<PsiElement> children = LatexPsiUtil.getAllChildren(displayMathElement);
        annotateMathCommands(children, annotationHolder, LatexSyntaxHighlighter.COMMAND_MATH_DISPLAY);
    }

    /**
     * Annotates the given comment.
     * @param comment The comment to annotate.
     */
    private void annotateComment(@NotNull PsiComment comment,
                                 @NotNull AnnotationHolder annotationHolder) {
        Annotation annotation = annotationHolder.createInfoAnnotation(comment, null);
        annotation.setTextAttributes(LatexSyntaxHighlighter.COMMENT);
    }

    /**
     * Annotates all command tokens of the comands that are included in the {@code elements}.
     *
     * @param elements
     *         All elements to handle. Only elements that are {@link LatexCommands} are considered.
     * @param highlighter
     *         The attributes to apply to all command tokens.
     */
    private void annotateMathCommands(@NotNull List<PsiElement> elements,
                                      @NotNull AnnotationHolder annotationHolder,
                                      @NotNull TextAttributesKey highlighter) {
        for (PsiElement element : elements) {
            if (!(element instanceof LatexCommands)) {
                continue;
            }

            PsiElement token = ((LatexCommands)element).getCommandToken();
            Annotation annotation = annotationHolder.createInfoAnnotation(token, null);
            annotation.setTextAttributes(highlighter);
        }
    }

    /**
     * Annotates the given optional parameters of commands.
     */
    private void annotateOptionalParameters(@NotNull LatexOptionalParam optionalParamElement,
                                            @NotNull AnnotationHolder annotationHolder) {
        for (PsiElement element : optionalParamElement.getOpenGroup().getContentList()) {
            if (!(element instanceof LatexContent)) {
                continue;
            }

            LatexContent content = (LatexContent)element;
            LatexNoMathContent noMathContent = content.getNoMathContent();
            if (noMathContent == null) {
                continue;
            }

            PsiElement toStyle = noMathContent.getNormalText();
            if (toStyle == null) {
                continue;
            }

            Annotation annotation = annotationHolder.createInfoAnnotation(toStyle, null);
            annotation.setTextAttributes(LatexSyntaxHighlighter.OPTIONAL_PARAM);
        }
    }

}
