package nl.rubensten.texifyidea.highlighting;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sten Wessel
 */
public class LatexPairedBraceMatcher implements PairedBraceMatcher {

    @Override
    public BracePair[] getPairs() {
        return new BracePair[]{
                new BracePair(LatexTypes.DISPLAY_MATH_START, LatexTypes.DISPLAY_MATH_END, true),
                new BracePair(LatexTypes.INLINE_MATH_START, LatexTypes.INLINE_MATH_END, false),
                new BracePair(LatexTypes.OPEN_BRACE, LatexTypes.CLOSE_BRACE, false),
                new BracePair(LatexTypes.OPEN_BRACKET, LatexTypes.CLOSE_BRACKET, false),
        };
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable
            IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
