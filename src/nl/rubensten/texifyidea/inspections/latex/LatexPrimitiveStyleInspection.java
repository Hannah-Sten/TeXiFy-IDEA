package nl.rubensten.texifyidea.inspections.latex;

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
import nl.rubensten.texifyidea.insight.InsightGroup;
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexMathContent;
import nl.rubensten.texifyidea.psi.LatexPsiUtil;
import nl.rubensten.texifyidea.util.DocumentsKt;
import nl.rubensten.texifyidea.util.Magic;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexPrimitiveStyleInspection extends TexifyInspectionBase {

    @NotNull
    @Override
    public InsightGroup getInspectionGroup() {
        return InsightGroup.LATEX;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Discouraged use of TeX styling primitives";
    }

    @NotNull
    @Override
    public String getInspectionId() {
        return "PrimitiveStyle";
    }

    @NotNull
    @Override
    public List<ProblemDescriptor> inspectFile(@NotNull PsiFile file, @NotNull InspectionManager
            manager, boolean isOntheFly) {
        List<ProblemDescriptor> descriptors = new SmartList<>();

        Collection<LatexCommands> commands = LatexCommandsIndex.Companion.getItems(file);
        for (LatexCommands command : commands) {
            int index = Magic.Command.stylePrimitives.indexOf(command.getName());
            if (index < 0) {
                continue;
            }

            descriptors.add(manager.createProblemDescriptor(
                    command,
                    "Use of TeX primitive " + Magic.Command.stylePrimitives.get(index) + " is discouraged",
                    new InspectionFix(),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
            ));
        }

        return descriptors;
    }

    private class InspectionFix implements LocalQuickFix {

        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Convert to LaTeX alternative";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            if (!(element instanceof LatexCommands)) {
                return;
            }

            // Find elements that go after the primitive.
            LatexCommands cmd = (LatexCommands)element;
            int cmdIndex = Magic.Command.stylePrimitives.indexOf(cmd.getName());
            if (cmdIndex < 0) {
                return;
            }

            PsiElement content = cmd.getParent().getParent();
            if (content instanceof LatexMathContent) {
                content = cmd.getParent();
            }
            PsiElement next = LatexPsiUtil.getNextSiblingIgnoreWhitespace(content);

            String after = (next == null ? "" : next.getText());
            String replacement = String.format(Magic.Command.stylePrimitveReplacements.get(cmdIndex), after);

            Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());

            // Delete the ending part..
            if (next != null) {
                DocumentsKt.deleteElement(document, next);
            }

            // Replace command.
            TextRange range = ((LatexCommands)element).getCommandToken().getTextRange();
            if (document != null) {
                document.replaceString(range.getStartOffset(), range.getEndOffset(), replacement);
            }
        }
    }
}
