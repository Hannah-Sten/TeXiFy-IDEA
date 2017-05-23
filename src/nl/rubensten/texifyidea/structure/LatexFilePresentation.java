package nl.rubensten.texifyidea.structure;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.TexifyIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public class LatexFilePresentation implements ItemPresentation {

    private final PsiFile file;

    public LatexFilePresentation(PsiFile file) {
        this.file = file;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return file.getName();
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
