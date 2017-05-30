package nl.rubensten.texifyidea.structure;

import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexOtherCommandPresentation implements EditableHintPresentation {

    private final String commandName;
    private final Icon icon;
    private String hint = "";

    public LatexOtherCommandPresentation(LatexCommands command, Icon icon) {
        this.commandName = command.getName();
        this.icon = icon;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return commandName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return hint;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return icon;
    }

    @Override
    public void setHint(@NotNull String hint) {
        this.hint = hint;
    }
}
