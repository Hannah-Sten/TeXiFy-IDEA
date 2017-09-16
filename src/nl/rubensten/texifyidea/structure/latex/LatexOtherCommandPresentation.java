package nl.rubensten.texifyidea.structure.latex;

import com.intellij.navigation.ItemPresentation;
import nl.rubensten.texifyidea.psi.LatexCommands;
import nl.rubensten.texifyidea.util.TexifyUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexOtherCommandPresentation implements ItemPresentation {

    private final String commandName;
    private final Icon icon;
    private final String locationString;

    public LatexOtherCommandPresentation(LatexCommands command, Icon icon) {
        this.commandName = command.getName();
        this.icon = icon;

        LatexCommands firstNext = TexifyUtil.getNextCommand(command);
        if (firstNext == null) {
            locationString = "";
            return;
        }

        String lookup = firstNext.getCommandToken().getText();
        this.locationString = lookup == null ? "" : lookup;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return commandName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return locationString;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return icon;
    }
}
