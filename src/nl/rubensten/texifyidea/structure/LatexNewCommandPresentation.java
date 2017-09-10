package nl.rubensten.texifyidea.structure;

import com.intellij.navigation.ItemPresentation;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexNewCommandPresentation implements ItemPresentation {

    private final String newCommandName;
    private final String locationString;

    public LatexNewCommandPresentation(LatexCommands newCommand) {
        // Fetch parameter amount.
        List<String> optional = newCommand.getOptionalParameters();
        int params = -1;
        if (!optional.isEmpty()) {
            try {
                params = Integer.parseInt(optional.get(0));
            }
            catch (NumberFormatException ignored) {
            }
        }
        String suffix = params != -1 ? "{x" + params + "}" : "";

        // Get command name.
        List<String> required = newCommand.getRequiredParameters();
        if (required.isEmpty()) {
            throw new IllegalArgumentException("\\newcommand has no command name");
        }
        this.newCommandName = required.get(0) + suffix;

        // Get value.
        if (required.size() > 1) {
            locationString = required.get(1);
        }
        else {
            locationString = "";
        }
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return newCommandName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return locationString;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_COMMAND;
    }
}
