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
public class LatexIncludePresentation implements ItemPresentation {

    private final String fileName;

    public LatexIncludePresentation(LatexCommands labelCommand) {
        if (!labelCommand.getCommandToken().getText().equals("\\include") &&
                !labelCommand.getCommandToken().getText().equals("\\includeonly") &&
                !labelCommand.getCommandToken().getText().equals("\\input")) {
            throw new IllegalArgumentException("command is no \\include(only)-command");
        }

        // Get label name.
        List<String> required = labelCommand.getRequiredParameters();
        if (required.isEmpty()) {
            throw new IllegalArgumentException("\\include(only) has no label name");
        }
        this.fileName = required.get(0);
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return fileName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return "";
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_INCLUDE;
    }
}
