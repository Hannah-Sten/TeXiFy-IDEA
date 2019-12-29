package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.PsiReference;
import nl.hannahsten.texifyidea.reference.BibtexStringReference;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used for method injection in generated classes.
 * Documentation can be found at https://github.com/JetBrains/Grammar-Kit/blob/master/HOWTO.md#34-implement-interface-via-method-injection
 */
public class BibtexPsiImplUtil {

    /**
     * Get a reference to the declaration of the string variable.
     */
    public static PsiReference getReference(@NotNull BibtexDefinedString element) {
        return new BibtexStringReference(element);
    }
}
