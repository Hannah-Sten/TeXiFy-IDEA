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
public class LatexSectionPresentation implements EditableHintPresentation {

    private final String sectionName;
    private String hint = "";

    public LatexSectionPresentation(LatexCommands sectionCommand) {
        if (!sectionCommand.getCommandToken().getText().equals("\\section")) {
            throw new IllegalArgumentException("command is no \\section-command");
        }

        this.sectionName = sectionCommand.getRequiredParameters().get(0);
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return sectionName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return hint;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_SECTION;
    }

    @Override
    public void setHint(@NotNull String hint) {
        this.hint = hint;
    }
}
