package nl.rubensten.texifyidea;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Sten Wessel
 */
public class LatexFileType extends LanguageFileType {

    public static final LatexFileType INSTANCE = new LatexFileType();

    private LatexFileType() {
        super(LatexLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "LaTeX source file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "LaTeX source file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "tex";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return TexifyIcons.LATEX_FILE;
    }
}
