package nl.rubensten.texifyidea.file;

import com.intellij.openapi.fileTypes.LanguageFileType;
import nl.rubensten.texifyidea.LatexLanguage;
import nl.rubensten.texifyidea.TexifyIcons;
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
