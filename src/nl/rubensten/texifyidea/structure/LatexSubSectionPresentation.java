package nl.rubensten.texifyidea.structure;

import com.intellij.navigation.ItemPresentation;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexSubSectionPresentation implements ItemPresentation {

    private final String subSectionName;

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
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_SUBSECTION;
    }
}
