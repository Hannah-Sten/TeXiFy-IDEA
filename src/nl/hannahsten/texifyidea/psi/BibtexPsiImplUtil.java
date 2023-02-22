package nl.hannahsten.texifyidea.psi;

import com.intellij.openapi.application.ActionsKt;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import kotlin.jvm.functions.Function0;
import nl.hannahsten.texifyidea.index.stub.BibtexEntryStub;
import nl.hannahsten.texifyidea.reference.BibtexStringReference;
import nl.hannahsten.texifyidea.util.BibtexKt;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * This class is used for method injection in generated classes.
 * Documentation can be found at <a href="https://github.com/JetBrains/Grammar-Kit/blob/master/HOWTO.md#34-implement-interface-via-method-injection">github.com</a>.
 * <p>
 * Note that it has to be written in Java.
 */
@SuppressWarnings("TypeMayBeWeakened")
public class BibtexPsiImplUtil {

    /**
     * Get a reference to the declaration of the string variable.
     */
    public static PsiReference getReference(@NotNull BibtexDefinedString element) {
        return new BibtexStringReference(element);
    }

    /*
     * BibtexEntry
     */

    public static boolean equals(@NotNull BibtexEntry element, Object other) {
        if (other == null) return false;
        if (other instanceof BibtexEntry otherBib) {
            return getName(element).equals(getName(otherBib)) &&
                    getTitle(element).equals(getTitle(otherBib)) &&
                    new HashSet<>(getAuthors(otherBib)).containsAll(getAuthors(element)) &&
                    new HashSet<>(getAuthors(element)).containsAll(getAuthors(otherBib)) &&
                    getYear(element).equals(getYear(otherBib)) &&
                    getAbstract(element).equals(getAbstract(otherBib));
        }
        else {
            return false;
        }
    }

    public static int hashCode(@NotNull BibtexEntry element) {
        return ActionsKt.runReadAction(() -> Objects.hash(element.getName(), element.getTitle(), element.getAuthors(), element.getYear(), element.getAbstract()));
    }

    public static PsiReference[] getReferences(@NotNull BibtexEntry element) {
        return BibtexEntryUtilKt.getReferences(element);
    }

    public static PsiElement setName(@NotNull BibtexEntry element, @NotNull @NonNls String name) {
        return element;
    }

    public static String getName(@NotNull BibtexEntry element) {
        BibtexEntryStub stub = element.getStub();
        if (stub != null) return stub.getName();
        return element.getIdentifier();
    }

    public static String getTitle(@NotNull BibtexEntry element) {
        BibtexEntryStub stub = element.getStub();
        if (stub != null) return stub.getTitle();
        return element.getTagContent("title");
    }

    public static List<String> getAuthors(@NotNull BibtexEntry element) {
        BibtexEntryStub stub = element.getStub();
        if (stub != null) return stub.getAuthors();
        String authorList = element.getTagContent("author");
        return Arrays.asList(authorList.split(" and "));
    }

    public static String getYear(@NotNull BibtexEntry element) {
        BibtexEntryStub stub = element.getStub();
        if (stub != null) return stub.getYear();

        return element.getTagContent("year");
    }

    public static String getIdentifier(@NotNull BibtexEntry element) {
        BibtexEntryStub stub = element.getStub();
        if (stub != null) return stub.getIdentifier();
        String identifier = BibtexKt.identifier(element);
        if (identifier == null) return "";
        return identifier;
    }

    public static PsiElement getNameIdentifier(@NotNull BibtexEntry element) {
        return element;
    }

    public static String getAbstract(@NotNull BibtexEntry element) {
        return element.getTagContent("abstract");
    }

    public static String getTagContent(@NotNull BibtexEntry element, String tagName) {
        return BibtexEntryUtilKt.getTagContent(element, tagName);
    }

    public static String toString(@NotNull BibtexEntry element) {
        return element.getText();
    }

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

    /*
     * BibtexTag
     */

    public static PsiReference[] getReferences(@NotNull BibtexTag element) {
        return BibtexTagUtilKt.getReferences(element);
    }
}
