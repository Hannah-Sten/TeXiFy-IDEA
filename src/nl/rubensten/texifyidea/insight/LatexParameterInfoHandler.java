package nl.rubensten.texifyidea.insight;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import nl.rubensten.texifyidea.lang.Argument;
import nl.rubensten.texifyidea.lang.LatexNoMathCommand;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author Sten Wessel
 */
public class LatexParameterInfoHandler implements ParameterInfoHandler<LatexCommands, LatexNoMathCommand> {

    @Nullable
    private static LatexCommands findLatexCommand(PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(element, LatexCommands.class);
    }

    @Override
    public boolean couldShowInLookup() {
        return true;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(LatexNoMathCommand p, ParameterInfoContext context) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Nullable
    @Override
    public LatexCommands findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        return findLatexCommand(context.getFile(), context.getOffset());
    }

    @Override
    public void showParameterInfo(@NotNull LatexCommands element, @NotNull CreateParameterInfoContext context) {
        Optional<LatexNoMathCommand> commandHuh =
                LatexNoMathCommand.get(element.getCommandToken().getText().substring(1));
        if (!commandHuh.isPresent()) {
            return;
        }

        context.setItemsToShow(new Object[] { commandHuh.get() });
        context.showHint(element, element.getTextOffset(), this);
    }

    @Nullable
    @Override
    public LatexCommands findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext
                                                                     context) {
        return findLatexCommand(context.getFile(), context.getOffset());
    }

    @Override
    public void updateParameterInfo(@NotNull LatexCommands element, @NotNull
            UpdateParameterInfoContext context) {
        context.setCurrentParameter(0);
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return "]}";
    }

    @Override
    public boolean tracksParameterIndex() {
        return true;
    }

    @Override
    public void updateUI(LatexNoMathCommand cmd, @NotNull ParameterInfoUIContext context) {
        if (cmd == null) {
            context.setUIComponentEnabled(false);
            return;
        }

        int index = context.getCurrentParameterIndex();
        Argument[] arguments = cmd.getArguments();

        if (index >= arguments.length) {
            context.setUIComponentEnabled(false);
            return;
        }

        context.setupUIComponentPresentation(cmd.getCommandDisplay() + cmd.getArgumentsDisplay(), 0, 0, false, false, true, context.getDefaultParameterColor());
    }
}
