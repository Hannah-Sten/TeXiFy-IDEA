package nl.rubensten.texifyidea.structure;

import nl.rubensten.texifyidea.TexifyIcons;
import nl.rubensten.texifyidea.psi.LatexCommands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexChapterPresentation implements EditableHintPresentation {

    private final String chapterName;
    private String hint = "";

    public LatexChapterPresentation(LatexCommands chapterCommand) {
        if (!chapterCommand.getCommandToken().getText().equals("\\chapter")) {
            throw new IllegalArgumentException("command is no \\chapter-command");
        }

        this.chapterName = chapterCommand.getRequiredParameters().get(0);
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return chapterName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return hint;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_CHAPTER;
    }

    @Override
    public void setHint(@NotNull String hint) {
        this.hint = hint;
    }
}
