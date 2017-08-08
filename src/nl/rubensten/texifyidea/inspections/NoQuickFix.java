package nl.rubensten.texifyidea.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ruben Schellekens
 */
public enum NoQuickFix implements LocalQuickFix {

    INSTANCE;

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        // Do absolutely nothing.
    }
}
