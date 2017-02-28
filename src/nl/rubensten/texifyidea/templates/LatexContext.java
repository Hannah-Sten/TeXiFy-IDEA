package nl.rubensten.texifyidea.templates;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.file.LatexFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class LatexContext extends TemplateContextType {

    protected LatexContext() {
        super("LATEX", "LaTeX");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        return file instanceof LatexFile;
    }
}
