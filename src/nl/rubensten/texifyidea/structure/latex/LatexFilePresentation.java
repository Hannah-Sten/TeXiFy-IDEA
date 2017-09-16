package nl.rubensten.texifyidea.structure.latex;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.TexifyIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.regex.Pattern;

/**
 * @author Ruben Schellekens
 */
public class LatexFilePresentation implements ItemPresentation {

    private static final Pattern EXTENSION = Pattern.compile("\\.[a-zA-Z0-9]+$");

    private final PsiFile file;
    private final String presentableText;

    public LatexFilePresentation(PsiFile file) {
        this.file = file;
        this.presentableText = EXTENSION.matcher(file.getName()).replaceAll("");
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return presentableText;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return file.getVirtualFile().getPath();
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return TexifyIcons.DOT_LATEX;
    }
}
