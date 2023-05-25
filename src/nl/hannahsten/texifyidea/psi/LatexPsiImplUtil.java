package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used for method injection in generated parser classes.
 * It has to be in Java for Grammar-Kit to be able to generate the parser classes correctly.
 */
public class LatexPsiImplUtil {


    /*
     * LatexParameterText
     */

    public static PsiReference[] getReferences(@NotNull LatexParameterText element) {
        return LatexParameterTextUtilKt.getReferences(element);
    }

    public static PsiReference getReference(@NotNull LatexParameterText element) {
        return LatexParameterTextUtilKt.getReference(element);
    }

    public static PsiElement getNameIdentifier(@NotNull LatexParameterText element) {
        return LatexParameterTextUtilKt.getNameIdentifier(element);
    }

    public static PsiElement setName(@NotNull LatexParameterText element, String name) {
        return LatexParameterTextUtilKt.setName(element, name);
    }

    public static String getName(@NotNull LatexParameterText element) {
        return LatexParameterTextUtilKt.getName(element);
    }


    public static void delete(@NotNull LatexParameterText element) {
        LatexParameterTextUtilKt.delete(element);
    }
    /*
     * LatexParameter
     */

    public static boolean isValidHost(@NotNull LatexParameter element) {
        return true;
    }

    public static PsiLanguageInjectionHost updateText(@NotNull LatexParameter element, @NotNull String text) {
        return ElementManipulators.handleContentChange(element, text);
    }

    @NotNull
    public static LiteralTextEscaper<LatexParameter> createLiteralTextEscaper(@NotNull LatexParameter element) {
        return LiteralTextEscaper.createSimple(element);
    }

}