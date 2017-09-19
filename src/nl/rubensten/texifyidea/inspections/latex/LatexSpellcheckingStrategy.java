package nl.rubensten.texifyidea.inspections.latex;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import nl.rubensten.texifyidea.LatexLanguage;
import nl.rubensten.texifyidea.lang.Argument;
import nl.rubensten.texifyidea.lang.Argument.Type;
import nl.rubensten.texifyidea.lang.LatexMathCommand;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.psi.LatexBeginCommand;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.psi.LatexEndCommand;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * @author Ruben Schellekens
 */
public class LatexSpellcheckingStrategy extends SpellcheckingStrategy {

    @Override
    public boolean isMyContext(@NotNull PsiElement psiElement) {
        return psiElement.getLanguage().equals(LatexLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement psiElement) {
        if (psiElement instanceof LeafPsiElement) {
            LeafPsiElement leaf = (LeafPsiElement)psiElement;

            if (leaf.getElementType().equals(LatexTypes.COMMAND_TOKEN) ||
                    leaf.getElementType().equals(LatexTypes.COMMENT_TOKEN) ||
                    isBeginEnd(psiElement)) {
                return EMPTY_TOKENIZER;
            }

            Argument argument = getArgument(leaf);

            if (argument == null && leaf.getElementType().equals(LatexTypes.NORMAL_TEXT)) {
                return TEXT_TOKENIZER;
            }

            if (argument == null || argument.getType() == Type.TEXT) {
                return TEXT_TOKENIZER;
            }
        }

        return EMPTY_TOKENIZER;
    }

    private boolean isBeginEnd(PsiElement element) {
        PsiElement elt = PsiTreeUtil.getParentOfType(element, LatexBeginCommand.class);
        if (elt != null) {
            return true;
        }

        elt = PsiTreeUtil.getParentOfType(element, LatexEndCommand.class);
        return elt != null;
    }

    private Argument getArgument(LeafPsiElement leaf) {
        LatexCommands parent = PsiTreeUtil.getParentOfType(leaf, LatexCommands.class);
        if (parent == null) {
            return null;
        }

        Argument[] arguments = getArguments(parent.getCommandToken().getText().substring(1));
        if (arguments == null) {
            return null;
        }

        List<String> realParams = parent.getRequiredParameters();
        int parameterIndex = realParams.indexOf(leaf.getText());
        if (parameterIndex < 0 || parameterIndex >= arguments.length) {
            return null;
        }

        return arguments[parameterIndex];
    }

    private Argument[] getArguments(String commandName) {
        Optional<LatexNoMathCommand> cmdHuh = LatexNoMathCommand.get(commandName);
        if (cmdHuh.isPresent()) {
            return cmdHuh.get().getArguments();
        }

        LatexMathCommand mathCmdHuh = LatexMathCommand.get(commandName);
        if (mathCmdHuh == null) {
            return null;
        }

        return mathCmdHuh.getArguments();
    }
}
