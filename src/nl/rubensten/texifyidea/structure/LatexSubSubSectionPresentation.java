package nl.rubensten.texifyidea.structure;

import com.intellij.navigation.ItemPresentation;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexSubSubSectionPresentation implements ItemPresentation {

    private final String subSubSectionName;

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
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_SUBSUBSECTION;
    }
}
