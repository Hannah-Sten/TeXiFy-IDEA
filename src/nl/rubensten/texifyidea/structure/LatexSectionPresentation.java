package nl.rubensten.texifyidea.structure;

import com.intellij.navigation.ItemPresentation;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexSectionPresentation implements ItemPresentation {

    private final String sectionName;

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
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_SECTION;
    }
}
