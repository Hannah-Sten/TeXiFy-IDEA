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
public class LatexSubSectionPresentation implements EditableHintPresentation {

    private final String subSectionName;
    private String hint = "";

    public LatexSubSectionPresentation(LatexCommands sectionCommand) {
        if (!sectionCommand.getCommandToken().getText().equals("\\subsection")) {
            throw new IllegalArgumentException("command is no \\subsection-command");
        }

        this.subSectionName = sectionCommand.getRequiredParameters().get(0);
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return subSectionName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return hint;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_SUBSECTION;
    }

    @Override
    public void setHint(@NotNull String hint) {
        this.hint = hint;
    }
}
