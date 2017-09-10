package nl.rubensten.texifyidea.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.completion.handlers.LatexReferenceInsertHandler;
import nl.rubensten.texifyidea.inspections.NonBreakingSpaceInspection;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

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
        final Collection<LatexCommands> labels = TexifyUtil.findLabels(project, key);
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
        String token = myElement.getCommandToken().getText();
        PsiFile file = myElement.getContainingFile().getOriginalFile();
        Collection<LatexCommands> labels = TexifyUtil.findLabels(file);

        labels.removeIf(label -> {
            String name = label.getName();
            return ("\\cite".equals(token) && "\\label".equals(name)) ||
                    (NonBreakingSpaceInspection.getREFERENCE_COMMANDS().contains(token) &&
                            "\\bibitem".equals(name) && !"\\cite".equals(token));
        });

        return labels.stream().map(
                l -> {
                    Icon icon = "\\bibitem".equals(l.getName()) ? TexifyIcons.DOT_BIB :
                            TexifyIcons.DOT_LABEL;

                    return LookupElementBuilder.create(l.getRequiredParameters().get(0))
                            .bold()
                            .withInsertHandler(new LatexReferenceInsertHandler())
                            .withTypeText(
                                    l.getContainingFile().getName() + ":" +
                                            (1 + StringUtil.offsetToLineNumber(l.getContainingFile().getText(), l.getTextOffset())),
                                    true
                            )
                            .withIcon(icon);
                }
        ).toArray();
    }
}
