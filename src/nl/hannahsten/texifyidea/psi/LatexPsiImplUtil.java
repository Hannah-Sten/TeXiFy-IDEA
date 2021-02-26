package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.*;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class is used for method injection in generated parser classes.
 * It has to be in Java for Grammar-Kit to be able to generate the parser classes correctly.
 */
public class LatexPsiImplUtil {

    /*
     * LatexCommands
     */

    /**
     * References which do not need a find usages to work on lower level psi elements (normal text) can be implemented on the command, otherwise they are in {@link LatexPsiImplUtil#getReference(LatexParameterText)}.
     * For more info and an example, see {@link nl.hannahsten.texifyidea.reference.LatexLabelParameterReference}.
     */
    @NotNull
    public static PsiReference[] getReferences(@NotNull LatexCommands element) {
        return LatexCommandsImplUtilKt.getReferences(element);
    }

    /**
     * Get the reference for this command, assuming it has exactly one reference (return null otherwise).
     */
    public static PsiReference getReference(@NotNull LatexCommands element) {
        PsiReference[] references = getReferences(element);
        if (references.length != 1) {
            return null;
        }
        else {
            return references[0];
        }
    }

    public static String toString(@NotNull LatexKeyvalKey element) {
        return LatexCommandsImplUtilKt.keyValContentToString(element);
    }

    public static String toString(@NotNull LatexKeyvalValue element) {
        return LatexCommandsImplUtilKt.keyValContentToString(element);
    }

    public static LinkedHashMap<LatexKeyvalKey, LatexKeyvalValue> getOptionalParameterMap(@NotNull LatexCommands element) {
        return LatexCommandsImplUtilKt.getOptionalParameterMap(element.getParameterList());
    }

    public static LinkedHashMap<LatexKeyvalKey, LatexKeyvalValue> getOptionalParameterMap(@NotNull LatexBeginCommand element) {
        return LatexCommandsImplUtilKt.getOptionalParameterMap(element.getParameterList());
    }


    /**
     * Generates a list of all names of all required parameters in the command.
     */
    public static List<String> getRequiredParameters(@NotNull LatexCommands element) {
        return LatexCommandsImplUtilKt.getRequiredParameters(element.getParameterList());
    }

    public static List<String> getRequiredParameters(@NotNull LatexBeginCommand element) {
        return LatexCommandsImplUtilKt.getRequiredParameters(element.getParameterList());
    }

    /**
     * Get the name of the command, for example \newcommand.
     */
    public static String getName(@NotNull LatexCommands element) {
        LatexCommandsStub stub = element.getStub();
        if (stub != null) return stub.getName();
        return element.getCommandToken().getText();
    }

    public static PsiElement setName(@NotNull LatexCommands element, String name) {
        return LatexCommandsImplUtilKt.setName(element, name);
    }

    /**
     * Checks if the command is followed by a label.
     */
    public static boolean hasLabel(@NotNull LatexCommands element) {
        return LatexCommandsImplUtilKt.hasLabel(element);
    }

    public static void delete(@NotNull LatexCommands element) {
        LatexCommandsImplUtilKt.delete(element);
    }

    /*
     * LatexEnvironment
     */

    public static String getLabel(@NotNull LatexEnvironment element) {
        return LatexEnvironmentUtilKt.getLabel(element);
    }

    public static String getEnvironmentName(@NotNull LatexEnvironment element) {
        return LatexEnvironmentUtilKt.getEnvironmentName(element);
    }

    public static boolean isValidHost(@NotNull LatexEnvironment element) {
        return true;
    }

    public static PsiLanguageInjectionHost updateText(@NotNull LatexEnvironment element, @NotNull String text) {
        return ElementManipulators.handleContentChange(element, text);
    }

    @NotNull
    public static LiteralTextEscaper<LatexEnvironment> createLiteralTextEscaper(@NotNull LatexEnvironment element) {
        return LiteralTextEscaper.createSimple(element);
    }

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