package nl.stenwessel.texifyidea.psi;

import com.intellij.psi.tree.IElementType;
import nl.stenwessel.texifyidea.LatexLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sten Wessel
 */
public class LatexElementType extends IElementType {

    public LatexElementType(@NotNull @NonNls String debugName) {
        super(debugName, LatexLanguage.INSTANCE);
    }
}
