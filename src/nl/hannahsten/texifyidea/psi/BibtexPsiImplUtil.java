package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used for method injection in generated classes.
 * Documentation can be found at <a href="https://github.com/JetBrains/Grammar-Kit/blob/master/HOWTO.md#34-implement-interface-via-method-injection">github.com</a>.
 * <p>
 * Note that it has to be written in Java.
 */
@SuppressWarnings("TypeMayBeWeakened")
public class BibtexPsiImplUtil {



    /*
     * BibtexId
     */

    public static PsiElement getNameIdentifier(@NotNull BibtexId element) {
        return BibtexIdUtilKt.getNameIdentifier(element);
    }

    public static PsiElement setName(@NotNull BibtexId element, String name) {
        return BibtexIdUtilKt.setName(element, name);
    }

    public static String getName(@NotNull BibtexId element) {
        return BibtexIdUtilKt.getName(element);
    }

    public static void delete(@NotNull BibtexId element) {
        BibtexIdUtilKt.delete(element);
    }

}
