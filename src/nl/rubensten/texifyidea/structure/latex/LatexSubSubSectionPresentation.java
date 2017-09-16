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
public class LatexSubSubSectionPresentation implements EditableHintPresentation {

    private final String subSubSectionName;
    private String hint = "";

    public LatexSubSubSectionPresentation(LatexCommands sectionCommand) {
        if (!sectionCommand.getCommandToken().getText().equals("\\subsubsection")) {
            throw new IllegalArgumentException("command is no \\subsubsection-command");
        }

        this.subSubSectionName = sectionCommand.getRequiredParameters().get(0);
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return subSubSectionName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return hint;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_SUBSUBSECTION;
    }

    @Override
    public void setHint(@NotNull String hint) {
        this.hint = hint;
    }
}
