package nl.rubensten.texifyidea.inspections.latex;

import com.google.common.collect.Sets;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import nl.rubensten.texifyidea.inspections.InspectionGroup;
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase;
import nl.rubensten.texifyidea.psi.LatexMathContent;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Detects non-escaped common math functions like <em>sin</em>, <em>cos</em> and replaces them
 * with {@code \sin}, {@code \cos}.
 *
 * @author Sten Wessel
 */
public class LatexMathOperatorEscapeInspection extends TexifyInspectionBase {

    private static Set<String> OPERATORS = Sets.newHashSet(
            "arccos", "arcsin", "arctan", "arg", "cos", "cosh", "cot", "coth", "csc",
            "deg", "det", "dim", "exp", "gcd", "hom", "inf", "ker", "lg", "lim", "liminf", "limsup",
            "ln", "log", "max", "min", "Pr", "sec", "sin", "sinh", "sup", "tan", "tanh"
    );

    @NotNull
    @Override
    public InspectionGroup getInspectionGroup() {
        return InspectionGroup.LATEX;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Non-escaped common math operators";
    }

    @NotNull
    @Override
    public String getInspectionId() {
        return "MathOperatorEscape";
    }

    @NotNull
    @Override
    public List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager manager,
                                               boolean isOnTheFly) {
        List<ProblemDescriptor> descriptors = new SmartList<>();

        PsiElementPattern.Capture<PsiElement> pattern = PlatformPatterns.psiElement(LatexTypes.NORMAL_TEXT_WORD);
        Collection<LatexMathContent> envs = PsiTreeUtil.findChildrenOfType(file, LatexMathContent.class);
        for (LatexMathContent env : envs) {
            env.acceptChildren(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    ProgressManager.checkCanceled();
                    if (pattern.accepts(element)) {
                        if (OPERATORS.contains(element.getText())) {
                            descriptors.add(manager.createProblemDescriptor(
                                    element,
                                    "Non-escaped math operator",
                                    new EscapeMathOperatorFix(),
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    isOnTheFly
                            ));
                        }
                    } else {
                        super.visitElement(element);
                    }
                }
            });
        }

        return descriptors;
    }

    private static class EscapeMathOperatorFix implements LocalQuickFix {

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Escape math operator";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
            if (document != null) {
                document.insertString(element.getTextOffset(), "\\");
            }
        }
    }
}
