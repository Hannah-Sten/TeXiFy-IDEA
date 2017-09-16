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
public class LatexSubParagraphPresentation implements EditableHintPresentation {

    private final String subParagraphName;
    private String hint = "";

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
        return hint;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_SUBPARAGRAPH;
    }

    @Override
    public void setHint(@NotNull String hint) {
        this.hint = hint;
    }
}
