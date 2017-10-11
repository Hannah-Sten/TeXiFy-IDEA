package nl.rubensten.texifyidea.inspections.latex;

import com.google.common.collect.Sets;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiFile;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.insight.InsightGroup;
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Sten Wessel
 */
public class LatexMightBreakTexifyInspection extends TexifyInspectionBase {

    private static final Set<String> REDEFINE_COMMANDS = Sets.newHashSet(
            "\\renewcommand", "\\def", "\\let"
    );

    private static final Set<String> FRAGILE_COMMANDS = Sets.newHashSet(
            "\\addtocounter", "\\begin", "\\chapter", "\\def", "\\documentclass", "\\end",
            "\\include", "\\includeonly", "\\input", "\\label", "\\let", "\\newcommand",
            "\\overline", "\\paragraph", "\\part", "\\renewcommand", "\\section", "\\setcounter",
            "\\sout", "\\subparagraph", "\\subsection", "\\subsubsection", "\\textbf",
            "\\textit", "\\textsc", "\\textsl", "\\texttt", "\\underline", "\\[", "\\]",
            "\\newenvironment", "\\bibitem"
    );

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
        List<ProblemDescriptor> descriptors = new SmartList<>();

        Collection<LatexCommands> commands = LatexCommandsIndex.Companion.getItems(file);
        for (LatexCommands command : commands) {
            // Error when \newcommand is used on existing command
            if (REDEFINE_COMMANDS.contains(command.getName())) {
                LatexCommands newCommand = TexifyUtil.getForcedFirstRequiredParameterAsCommand(command);
                if (newCommand == null) {
                    continue;
                }

                if (FRAGILE_COMMANDS.contains(newCommand.getName())) {
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
