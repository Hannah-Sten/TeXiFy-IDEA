package nl.rubensten.texifyidea.structure;

import com.intellij.navigation.ItemPresentation;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexParagraphPresentation implements ItemPresentation {

    private final String paragraphName;

    public LatexParagraphPresentation(LatexCommands paragraphCommand) {
        if (!paragraphCommand.getCommandToken().getText().equals("\\paragraph")) {
            throw new IllegalArgumentException("command is no \\paragraph-command");
        }

        if (paragraphCommand.getRequiredParameters().isEmpty()) {
            this.paragraphName = "";
        }
        else {
            this.paragraphName = paragraphCommand.getRequiredParameters().get(0);
        }
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return paragraphName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_PARAGRAPH;
    }
}
