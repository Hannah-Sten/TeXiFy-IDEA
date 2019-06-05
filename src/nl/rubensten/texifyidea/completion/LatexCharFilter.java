package nl.rubensten.texifyidea.completion;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.file.LatexFile;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sten Wessel
 */
public class LatexCharFilter extends CharFilter {

    @Nullable
    @Override
    public Result acceptChar(char c, int prefixLength, Lookup lookup) {
        if (!isInLatexContext(lookup)) {
            return null;
        }

        switch (c) {
            case '$': return Result.HIDE_LOOKUP;
            case ':': return Result.ADD_TO_PREFIX;
            default: return null;
        }
    }

    private static boolean isInLatexContext(Lookup lookup) {
        if (!lookup.isCompletion()) {
            return false;
        }

        PsiElement element = lookup.getPsiElement();
        PsiFile file = lookup.getPsiFile();

        return file instanceof LatexFile && element != null;
    }
}
