package nl.rubensten.texifyidea.psi;

import com.intellij.psi.tree.IElementType;
import nl.rubensten.texifyidea.LatexLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class LatexTokenType extends IElementType {
    public LatexTokenType(@NotNull @NonNls String debugName) {
        super(debugName, LatexLanguage.INSTANCE);
    }
}
