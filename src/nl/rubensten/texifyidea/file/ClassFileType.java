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
public class ClassFileType extends LanguageFileType {

    public static final ClassFileType INSTANCE = new ClassFileType();

    private ClassFileType() {
        super(LatexLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "LaTeX class file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "LaTeX class file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "cls";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return TexifyIcons.CLASS_FILE;
    }
}
