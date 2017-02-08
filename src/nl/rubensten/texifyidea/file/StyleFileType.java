package nl.rubensten.texifyidea.file;

import com.intellij.openapi.fileTypes.LanguageFileType;
import nl.rubensten.texifyidea.LatexLanguage;
import nl.rubensten.texifyidea.TexifyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class StyleFileType extends LanguageFileType {

    public static final StyleFileType INSTANCE = new StyleFileType();

    private StyleFileType() {
        super(LatexLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "LaTeX style file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "LaTeX style file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "sty";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return TexifyIcons.STYLE_FILE;
    }
}
