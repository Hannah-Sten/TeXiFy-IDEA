package nl.rubensten.texifyidea.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sten Wessel
 */
public class ReplaceInlineMathToRobustInspection extends LocalInspectionTool {

    private static void checkFile(final PsiFile file, final ProblemsHolder problemsHolder) {
        file.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element.getNode() == LatexTypes.INLINE_MATH_START) {
                    // TODO
                }
            }
        });
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "LaTeX";
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Replace inline math to robust";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "ReplaceInlineMathToRobust";
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager
            manager, boolean isOnTheFly) {
        ProblemsHolder problemsHolder = new ProblemsHolder(manager, file, isOnTheFly);
        checkFile(file, problemsHolder);
        return problemsHolder.getResultsArray();
    }
}
