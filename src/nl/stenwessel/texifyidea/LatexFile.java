package nl.stenwessel.texifyidea;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Sten Wessel
 */
public class LatexFile extends PsiFileBase {

    public LatexFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, LatexLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return LatexFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Latex File";
    }

    @Nullable
    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }
}
