package nl.rubensten.texifyidea.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexMathEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sten Wessel
 */
public class LatexMathSymbolFoldingBuilder extends FoldingBuilderEx {

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document
            document, boolean quick) {
        FoldingGroup group = FoldingGroup.newGroup("latexMathSymbol");

        List<FoldingDescriptor> descriptors = new ArrayList<>();
        Collection<LatexMathEnvironment> mathEnvs = PsiTreeUtil.findChildrenOfType(root, LatexMathEnvironment.class);

        for (LatexMathEnvironment mathEnv : mathEnvs) {
            Collection<LatexCommands> commands = PsiTreeUtil.findChildrenOfType(mathEnv, LatexCommands.class);
            for (LatexCommands command : commands) {
                LatexMathCommand c = LatexMathCommand.get(command.getCommandToken().getText().substring(1));
                if (c != null) {
                    descriptors.add(new FoldingDescriptor(
                            command.getCommandToken().getNode(),
                            command.getCommandToken().getTextRange(),
                            group
                    ) {
                        @Nullable
                        @Override
                        public String getPlaceholderText() {
                            return c.getDisplay();
                        }
                    });
                }
            }
        }

        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return true;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        return null;
    }
}
