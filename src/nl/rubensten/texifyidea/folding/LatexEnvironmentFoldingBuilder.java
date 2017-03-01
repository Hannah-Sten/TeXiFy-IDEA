package nl.rubensten.texifyidea.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.psi.LatexEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Adds folding regions for LaTeX environments.
 * <p>
 * Enables folding of {@code \begin{environment} ... \end{environment}}.
 *
 * @author Sten Wessel
 */
public class LatexEnvironmentFoldingBuilder extends FoldingBuilderEx {

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document
            document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        Collection<LatexEnvironment> envs = PsiTreeUtil.findChildrenOfType(root,
                                                                           LatexEnvironment.class);

        for (LatexEnvironment env : envs) {
            // Get content offsets.
            // Uses the commands instead of the actual contents as they may be empty.
            int start = env.getBeginCommand().getTextRange().getEndOffset();
            int end = env.getEndCommand().getTextRange().getStartOffset();

            descriptors.add(new FoldingDescriptor(env, new TextRange(start, end)));
        }

        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        return "...";
    }
}
