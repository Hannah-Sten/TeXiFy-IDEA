package nl.rubensten.texifyidea.highlighting;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import nl.rubensten.texifyidea.psi.LatexContent;
import nl.rubensten.texifyidea.psi.LatexDisplayMath;
import nl.rubensten.texifyidea.psi.LatexInlineMath;
import nl.rubensten.texifyidea.psi.LatexOptionalParam;
import org.jetbrains.annotations.NotNull;

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
    }

    private void annotateInlineMath(@NotNull LatexInlineMath inlineMathElement,
                                    @NotNull AnnotationHolder annotationHolder) {
        Annotation annotation = annotationHolder.createInfoAnnotation(inlineMathElement, null);
        annotation.setTextAttributes(LatexSyntaxHighlighter.INLINE_MATH);
    }

    private void annotateDisplayMath(@NotNull LatexDisplayMath displayMathElement,
                                     @NotNull AnnotationHolder annotationHolder) {
        Annotation annotation = annotationHolder.createInfoAnnotation(displayMathElement, null);
        annotation.setTextAttributes(LatexSyntaxHighlighter.DISPLAY_MATH);
    }

    private void annotateOptionalParameters(@NotNull LatexOptionalParam optionalParamElement,
                                            @NotNull AnnotationHolder annotationHolder) {
        for (PsiElement element : optionalParamElement.getOpenGroup().getContentList()) {
            if (!(element instanceof LatexContent)) {
                continue;
            }

            LatexContent content = (LatexContent)element;
            PsiElement toStyle = content.getNoMathContent().getNormalText();

            Annotation annotation = annotationHolder.createInfoAnnotation(toStyle, null);
            annotation.setTextAttributes(LatexSyntaxHighlighter.OPTIONAL_PARAM);
        }
    }

}
