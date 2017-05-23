package nl.rubensten.texifyidea.structure;

import com.intellij.navigation.ItemPresentation;
import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexSubParagraphPresentation implements ItemPresentation {

    private final String subParagraphName;

    public LatexSubParagraphPresentation(LatexCommands subParagraphCommand) {
        if (!subParagraphCommand.getCommandToken().getText().equals("\\subparagraph")) {
            throw new IllegalArgumentException("command is no \\subparagraph-command");
        }

        this.subParagraphName = subParagraphCommand.getRequiredParameters().get(0);
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return subParagraphName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_SUBPARAGRAPH;
    }
}
