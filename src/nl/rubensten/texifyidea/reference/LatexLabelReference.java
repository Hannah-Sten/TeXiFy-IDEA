package nl.rubensten.texifyidea.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexRequiredParam;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Sten Wessel
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
        Project project = myElement.getProject();
        Collection<LatexCommands> labels = TexifyUtil.findLabels(project);

        return labels.stream().map(
                l -> LookupElementBuilder.create(l)
                        .withTypeText(l.getContainingFile().getName())
        ).toArray();
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return super.getCanonicalText();
    }
}
