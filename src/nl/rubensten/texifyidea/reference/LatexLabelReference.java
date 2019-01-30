package nl.rubensten.texifyidea.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.completion.handlers.LatexReferenceInsertHandler;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import nl.rubensten.texifyidea.settings.TexifySettings;
import nl.rubensten.texifyidea.util.LabelsKt;
import nl.rubensten.texifyidea.util.Magic;
import nl.rubensten.texifyidea.util.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        Map<String, Integer> commands = TexifySettings.getInstance().getLabelCommandsLeadingSlash();

        String command = myElement.getCommandToken().getText();

        // add bibreferences to autocompletion for \cite-style commands
        if (Magic.Command.bibliographyReference.contains(command)) {
            return LabelsKt.findBibtexItems(file).stream()
                    .map(bibtexId -> {
                        if (bibtexId != null) {
                            PsiFile containing = bibtexId.getContainingFile();
                            return LookupElementBuilder.create(StringsKt.substringEnd(bibtexId.getText(), 1))
                                    .bold()
                                    .withInsertHandler(new LatexReferenceInsertHandler())
                                    .withTypeText(containing.getName() + ": " +
                                                    (1 + StringUtil.offsetToLineNumber(containing.getText(), bibtexId.getTextOffset())),
                                            true)
                                    .withIcon(TexifyIcons.DOT_BIB);
                        }
                        return null;
                    }).filter(Objects::nonNull).toArray();
        }
        // add all labels to \ref-styled commands
        else if (Magic.Command.labelReference.contains(command)) {
            return LabelsKt.findLabelingCommands(file).stream().map(labelingCommand -> {
                if (labelingCommand != null) {
                    List<String> parameters = labelingCommand.getRequiredParameters();
                    if (parameters != null && parameters.size() >= commands.get(labelingCommand.getName())) {
                        String label = parameters.get(commands.get(labelingCommand.getName()) - 1);
                        return LookupElementBuilder.create(label)
                                .bold()
                                .withInsertHandler(new LatexReferenceInsertHandler())
                                .withTypeText(labelingCommand.getContainingFile().getName() + ":"
                                        + (1 + StringUtil.offsetToLineNumber(
                                        labelingCommand.getContainingFile().getText(),
                                        labelingCommand.getTextOffset())), true)
                                .withIcon(TexifyIcons.DOT_LABEL);
                    }
                }
                return null;
            }).filter(Objects::nonNull).toArray();
        }
        // if command isn't ref or cite-styled return empty array
        return new Object[]{};
    }
}
