package nl.hannahsten.texifyidea.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import nl.hannahsten.texifyidea.TexifyIcons;
import nl.hannahsten.texifyidea.completion.handlers.LatexReferenceInsertHandler;
import nl.hannahsten.texifyidea.psi.BibtexId;
import nl.hannahsten.texifyidea.psi.LatexCommands;
import nl.hannahsten.texifyidea.psi.LatexRequiredParam;
import nl.hannahsten.texifyidea.util.LabelsKt;
import nl.hannahsten.texifyidea.util.Magic;
import nl.hannahsten.texifyidea.util.StringsKt;
import nl.hannahsten.texifyidea.util.files.FileSetKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Hannah Schellekens, Sten Wessel
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
        String token = myElement.getCommandToken().getText();
        PsiFile file = myElement.getContainingFile().getOriginalFile();
        Collection<PsiElement> labels = new ArrayList<>();
        for (PsiFile referenced : FileSetKt.referencedFileSet(file)) {
            labels.addAll(LabelsKt.findLabels(referenced));
        }

        labels.removeIf(label -> {
            if (label instanceof LatexCommands) {
                String name = ((LatexCommands)label).getName();
                return (Magic.Command.bibliographyReference.contains(token) && "\\label".equals(name)) ||
                        (Magic.Command.reference.contains(token) &&
                                "\\bibitem".equals(name) && !Magic.Command.bibliographyReference.contains(token));
            }

            return false;
        });

        return labels.stream().map(
                l -> {
                    if (l instanceof LatexCommands) {
                        LatexCommands cmd = (LatexCommands)l;
                        Icon icon = "\\bibitem".equals(cmd.getName()) ?
                                TexifyIcons.DOT_BIB :
                                TexifyIcons.DOT_LABEL;

                        if (cmd.getRequiredParameters().isEmpty()) {
                            return null;
                        }

                        return LookupElementBuilder.create(cmd.getRequiredParameters().get(0))
                                .bold()
                                .withInsertHandler(new LatexReferenceInsertHandler())
                                .withTypeText(
                                        l.getContainingFile().getName() + ":" +
                                                (1 + StringUtil.offsetToLineNumber(l.getContainingFile().getText(), l.getTextOffset())),
                                        true
                                )
                                .withIcon(icon);
                    }
                    else if (Magic.Command.bibliographyReference.contains(token)) {
                        BibtexId id = (BibtexId)l;
                        PsiFile containing = id.getContainingFile();
                        String text = StringsKt.substringEnd(id.getText(), 1);
                        return LookupElementBuilder.create(text)
                                .bold()
                                .withInsertHandler(new LatexReferenceInsertHandler())
                                .withTypeText(
                                        containing.getName() + ":" +
                                                (1 + StringUtil.offsetToLineNumber(
                                                        containing.getText(), id.getTextOffset()
                                                )), true)
                                .withIcon(TexifyIcons.DOT_BIB);
                    }

                    return null;
                }
        ).filter(Objects::nonNull).toArray();
    }
}
