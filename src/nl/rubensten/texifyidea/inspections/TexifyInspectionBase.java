package nl.rubensten.texifyidea.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import nl.rubensten.texifyidea.insight.InsightGroup;
import nl.rubensten.texifyidea.util.PsiUtilKt;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Sten Wessel
 */
public abstract class TexifyInspectionBase extends LocalInspectionTool {

    @NotNull
    public abstract InsightGroup getInspectionGroup();

    @NotNull
    public abstract String getInspectionId();

    @Nls
    @NotNull
    @Override
    public abstract String getDisplayName();

    @NotNull
    public abstract List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOntheFly);

    @NotNull
    @Override
    public String getShortName() {
        return getInspectionGroup().getPrefix() + getInspectionId();
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return getInspectionGroup().getDisplayName();
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        InsightGroup group = getInspectionGroup();
        if (!group.getFileTypes().contains(file.getFileType())) {
            return null;
        }

        List<ProblemDescriptor> descriptors = inspectFile(file, manager, isOnTheFly);
        descriptors.removeIf(descriptor -> !checkContext(descriptor.getPsiElement()));
        return descriptors.toArray(new ProblemDescriptor[descriptors.size()]);
    }

    /**
     * Checks if the element is in the correct context.
     * <p>
     * By default returns `false` when in comments.
     *
     * @return `true` if the inspection is allowed in the context, `false` otherwise.
     */
    public boolean checkContext(@NotNull PsiElement element) {
        return !PsiUtilKt.isComment(element);
    }

    /**
     * Creates an empty list to store problem descriptors in.
     */
    protected List<ProblemDescriptor> descriptorList() {
        return new SmartList<>();
    }
}
