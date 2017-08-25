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
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import nl.rubensten.texifyidea.index.LatexCommandsIndex;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexMathContent;
import nl.rubensten.texifyidea.psi.LatexPsiUtil;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class OverInsteadOfFracInspection extends TexifyInspectionBase {

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Discouraged use of \\over";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "OverInsteadOfFrac";
    }

    @NotNull
    @Override
    List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager
            manager, boolean isOntheFly) {
        List<ProblemDescriptor> descriptors = new SmartList<>();

        Collection<LatexCommands> commands = LatexCommandsIndex.getIndexCommands(file);
        for (LatexCommands command : commands) {
            if ("\\over".equals(command.getName())) {
                descriptors.add(manager.createProblemDescriptor(
                        command,
                        "Use of \\over is discouraged",
                        new OverToFracFix(),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                ));
            }
        }

        return descriptors;
    }

    private class OverToFracFix implements LocalQuickFix {

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Convert to \\frac";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            if (!(element instanceof LatexCommands)) {
                return;
            }

            // Find elements to put in numerator and denominator.
            LatexCommands cmd = (LatexCommands)element;
            PsiElement content = cmd.getParent().getParent();
            if (content instanceof LatexMathContent) {
                content = cmd.getParent();
            }
            PsiElement previous = LatexPsiUtil.getPreviousSiblingIgnoreWhitespace(content);
            PsiElement next = LatexPsiUtil.getNextSiblingIgnoreWhitespace(content);

            String before = (previous == null ? "" : previous.getText());
            String after = (next == null ? "" : next.getText());
            String replacement = String.format("\\frac{%s}{%s}", before, after);

            Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());

            // Delete denominator.
            if (next != null) {
                TexifyUtil.deleteElement(document, next);
            }

            // Replace command.
            TextRange range = ((LatexCommands)element).getCommandToken().getTextRange();
            if (document != null) {
                document.replaceString(range.getStartOffset(), range.getEndOffset(), replacement);
            }

            // Replace numerator.
            if (previous != null) {
                TexifyUtil.deleteElement(document, previous);
            }
        }
    }
}
