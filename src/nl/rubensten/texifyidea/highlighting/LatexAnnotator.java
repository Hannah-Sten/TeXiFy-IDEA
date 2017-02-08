package nl.rubensten.texifyidea.highlighting;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import nl.rubensten.texifyidea.LatexSyntaxHighlighter;
import nl.rubensten.texifyidea.psi.LatexDisplayMath;
import nl.rubensten.texifyidea.psi.LatexInlineMath;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ruben Schellekens
 */
public class LatexAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof LatexInlineMath) {
            annotateInlineMath((LatexInlineMath)psiElement, annotationHolder);
        }
        else if (psiElement instanceof LatexDisplayMath) {
            annotateDisplayMath((LatexDisplayMath)psiElement, annotationHolder);
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

}
