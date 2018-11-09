package nl.rubensten.texifyidea.inspections.latex;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.insight.InsightGroup;
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.util.Magic;
import nl.rubensten.texifyidea.util.PsiCommandsKt;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Sten Wessel
 */
public class LatexMightBreakTexifyInspection extends TexifyInspectionBase {

    @NotNull
    @Override
    public InsightGroup getInspectionGroup() {
        return InsightGroup.LATEX;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Might break TeXiFy functionality";
    }

    @NotNull
    @Override
    public String getInspectionId() {
        return "MightBreakTexify";
    }

    @NotNull
    @Override
    public List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager
            manager, boolean isOntheFly) {
        List<ProblemDescriptor> descriptors = descriptorList();

        Collection<LatexCommands> commands = LatexCommandsIndex.Companion.getItems(file);
        for (LatexCommands command : commands) {
            // Error when \newcommand is used on existing command
            if (Magic.Command.redefinitions.contains(command.getName())) {
                LatexCommands newCommand = PsiCommandsKt.forcedFirstRequiredParameterAsCommand(command);
                if (newCommand == null) {
                    continue;
                }

                if (Magic.Command.fragile.contains(newCommand.getName())) {
                    descriptors.add(manager.createProblemDescriptor(
                            command,
                            "This might break TeXiFy functionality",
                            (LocalQuickFix)null,
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly
                    ));
                }
            }
        }

        return descriptors;
    }
}
