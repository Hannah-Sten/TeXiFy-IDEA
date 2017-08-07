package nl.rubensten.texifyidea.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import nl.rubensten.texifyidea.psi.LatexMathEnvironment;
import nl.rubensten.texifyidea.psi.LatexNormalText;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sten Wessel
 */
public class UnicodeInspection extends TexifyInspectionBase {

    private static final Pattern NONASCII_PATTERN = Pattern.compile("\\P{ASCII}");

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Unsupported Unicode character";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "Unicode";
    }

    @NotNull
    @Override
    List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager
            manager, boolean isOntheFly) {
        List<ProblemDescriptor> descriptors = new SmartList<>();

        Collection<LatexNormalText> texts = PsiTreeUtil.findChildrenOfType(file, LatexNormalText.class);
        for (LatexNormalText text : texts) {
            Matcher matcher = NONASCII_PATTERN.matcher(text.getText());
            while (matcher.find()) {
                boolean inMathMode = PsiTreeUtil.getParentOfType(text, LatexMathEnvironment.class) != null;

                descriptors.add(manager.createProblemDescriptor(
                        text,
                        new TextRange(matcher.start(), matcher.end()),
                        "Unsupported Unicode character",
                        ProblemHighlightType.ERROR,
                        isOntheFly,
                        new InsertUnicodePackageFix(inMathMode)
                ));
            }
        }

        return descriptors;
    }

    private static class InsertUnicodePackageFix implements LocalQuickFix {

        private boolean inMathMode;

        InsertUnicodePackageFix(boolean inMathMode) {
            this.inMathMode = inMathMode;
        }

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return inMathMode ? "Include Unicode math support package" : "Include Unicode support package";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            if (inMathMode) {
                descriptor.getTextRangeInElement();
            } else {

            }
        }
    }
}
