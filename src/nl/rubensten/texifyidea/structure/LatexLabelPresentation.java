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
public class LatexLabelPresentation implements ItemPresentation {

    private final String labelName;

    public LatexLabelPresentation(LatexCommands labelCommand) {
        if (!labelCommand.getCommandToken().getText().equals("\\label")) {
            throw new IllegalArgumentException("command is no \\label-command");
        }

        // Get label name.
        List<String> required = labelCommand.getRequiredParameters();
        if (required.isEmpty()) {
            throw new IllegalArgumentException("\\label has no label name");
        }
        this.labelName = required.get(0);
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return labelName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return "";
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_LABEL;
    }
}
