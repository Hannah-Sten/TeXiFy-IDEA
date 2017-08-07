package nl.rubensten.texifyidea.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Sten Wessel
 */
public class CommandAlreadyDefinedInspection extends TexifyInspectionBase {

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Command is already defined";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "CommandAlreadyDefined";
    }

    @NotNull
    @Override
    List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager
            manager, boolean isOntheFly) {
        List<ProblemDescriptor> descriptors = new SmartList<>();

        Collection<LatexCommands> commands = PsiTreeUtil.findChildrenOfType(file, LatexCommands.class);
        for (LatexCommands command : commands) {
            // Error when \newcommand is used on existing command
            if ("\\newcommand".equals(command.getName())) {
                LatexCommands newCommand = TexifyUtil.getForcedFirstRequiredParameterAsCommand(command);
                if (newCommand == null) {
                    continue;
                }

                if (TexifyUtil.isCommandKnown(newCommand)) {
                    descriptors.add(manager.createProblemDescriptor(
                            command,
                            newCommand.getCommandToken().getTextRange().shiftRight(-command.getTextOffset()),
                            "Command is already defined",
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly,
                            new RenewCommandFix()
                    ));
                }
            }
            // Warning when a builtin command gets overridden
            else if ("\\def".equals(command.getName())) {
                LatexCommands newCommand = TexifyUtil.getForcedFirstRequiredParameterAsCommand(command);
                if (newCommand == null) {
                    continue;
                }

                if (TexifyUtil.isCommandKnown(newCommand)) {
                    descriptors.add(manager.createProblemDescriptor(
                            command,
                            newCommand.getCommandToken().getTextRange().shiftRight(-command.getTextOffset()),
                            "Command is already defined",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                    ));
                }
            }

            // TODO: check for user-defined duplicate commands.
        }

        return descriptors;
    }

    private class RenewCommandFix implements LocalQuickFix {

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Convert to \\renewcommand";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            if (!(element instanceof LatexCommands)) {
                return;
            }

            TextRange range = ((LatexCommands)element).getCommandToken().getTextRange();

            Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
            if (document != null) {
                document.replaceString(range.getStartOffset(), range.getEndOffset(), "\\renewcommand");
            }
        }
    }
}
