package nl.rubensten.texifyidea.structure.latex;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.structure.EditableHintPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexPartPresentation implements EditableHintPresentation {

    private final String partName;
    private String hint = "";

    public LatexPartPresentation(LatexCommands partCommand) {
        if (!partCommand.getCommandToken().getText().equals("\\part")) {
            throw new IllegalArgumentException("command is no \\part-command");
        }

        this.partName = partCommand.getRequiredParameters().get(0);
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return partName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return hint;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_PART;
    }

    @Override
    public void setHint(@NotNull String hint) {
        this.hint = hint;
    }
}
