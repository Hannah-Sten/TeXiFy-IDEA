package nl.rubensten.texifyidea.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import nl.rubensten.texifyidea.file.LatexFile;
import nl.rubensten.texifyidea.psi.LatexInlineMath;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Not yet ready for production, only for demonstration.
 *
 * @author Sten Wessel
 */
public class ReplaceInlineMathToRobustInspection extends TexifyInspectionBase {

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
        if (!(file instanceof LatexFile)) {
            return null;
        }

        List<ProblemDescriptor> descriptors = new SmartList<>();

        Collection<LatexInlineMath> envs = PsiTreeUtil.findChildrenOfType(file, LatexInlineMath.class);
        for (LatexInlineMath env : envs) {
            ProgressManager.checkCanceled();
            descriptors.add(manager.createProblemDescriptor(env, "Change to robust environment", (LocalQuickFix)null, ProblemHighlightType.WEAK_WARNING, true));
        }

        return descriptors.toArray(new ProblemDescriptor[descriptors.size()]);
    }
}
