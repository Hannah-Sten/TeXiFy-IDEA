package nl.rubensten.texifyidea.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.completion.handlers.LatexReferenceInsertHandler;
import nl.rubensten.texifyidea.psi.BibtexId;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import nl.rubensten.texifyidea.settings.TexifySettings;
import nl.rubensten.texifyidea.util.FileSetKt;
import nl.rubensten.texifyidea.util.LabelsKt;
import nl.rubensten.texifyidea.util.Magic;
import nl.rubensten.texifyidea.util.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ruben Schellekens, Sten Wessel
 */
public class LatexLabelReference extends PsiReferenceBase<LatexCommands> implements PsiPolyVariantReference {

    private String key;

    public LatexLabelReference(@NotNull LatexCommands element, LatexRequiredParam param) {
        super(element);
        key = param.getText().substring(1, param.getText().length() - 1);

        // Only show Ctrl+click underline under the reference name
        setRangeInElement(new TextRange(param.getTextOffset() - element.getTextOffset() + 1, param.getTextOffset() - element.getTextOffset() + key.length() + 1));
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        Project project = myElement.getProject();
        final Collection<PsiElement> labels = LabelsKt.findLabels(project, key);
        return labels.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        PsiFile file = myElement.getContainingFile().getOriginalFile();
        Collection<PsiElement> labelingCommands = new ArrayList<>();
        Map<String, Integer> commands = TexifySettings.getInstance().getLabelCommandsLeadingSlash();

        for (PsiFile referenced : FileSetKt.referencedFileSet(file)) {
            labelingCommands.addAll(LabelsKt.findLabelingCommands(referenced));
        }

        return labelingCommands.stream().map(labelingCommand -> {
            if (labelingCommand instanceof LatexCommands) {
                LatexCommands command = (LatexCommands) labelingCommand;
                List<String> parameters = command.getRequiredParameters();
                if (parameters != null && parameters.size() >= commands.get(command.getName())) {
                    String label = parameters.get(commands.get(command.getName()) - 1);
                    Icon icon = (command.getName() != null && command.getName().contains("bib")) ?
                            TexifyIcons.DOT_BIB : TexifyIcons.DOT_LABEL;
                    return LookupElementBuilder.create(label)
                            .bold()
                            .withInsertHandler(new LatexReferenceInsertHandler())
                            .withTypeText(labelingCommand.getContainingFile().getName() + ":"
                                    + (1 + StringUtil.offsetToLineNumber(
                                            labelingCommand.getContainingFile().getText(),
                                            labelingCommand.getTextOffset())), true)
                            .withIcon(icon);
                }
            }
            return null;
        }).filter(Objects::nonNull).toArray();
    }
}
