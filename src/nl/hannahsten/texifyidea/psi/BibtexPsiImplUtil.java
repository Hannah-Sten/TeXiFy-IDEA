package nl.hannahsten.texifyidea.psi;

import com.intellij.openapi.paths.WebReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import nl.hannahsten.texifyidea.index.stub.BibtexEntryStub;
import nl.hannahsten.texifyidea.reference.BibtexStringReference;
import nl.hannahsten.texifyidea.util.BibtexKt;
import nl.hannahsten.texifyidea.util.Magic;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is used for method injection in generated classes.
 * Documentation can be found at https://github.com/JetBrains/Grammar-Kit/blob/master/HOWTO.md#34-implement-interface-via-method-injection.
 */
public class BibtexPsiImplUtil {
    static final Set<String> URL_COMMANDS = Magic.Command.bibUrls;

    /**
     * Get a reference to the declaration of the string variable.
     */
    public static PsiReference getReference(@NotNull BibtexDefinedString element) {
        return new BibtexStringReference(element);
    }

    public static PsiReference[] getReferences(@NotNull BibtexEntry element) {
        if (URL_COMMANDS.stream().anyMatch(urlTag -> !element.getTagContent(urlTag).isEmpty())) {
            String contentText = element.getEntryContent().getText();
            List<TextRange> rangesInParent = URL_COMMANDS.stream().map(urlTag ->
                    TextRange.from(contentText.indexOf(element.getTagContent(urlTag)), element.getTagContent(urlTag).length())
            ).collect(Collectors.toList());

            List<PsiReference> references = new ArrayList<>();
            for (TextRange range : rangesInParent) {
                references.add(new WebReference(
                        element, range.shiftRight(element.getEntryContent().getTextOffset() - element.getTextOffset())
                ));
            }
            return references.toArray(new PsiReference[references.size()]);
        }
        return new PsiReference[0];
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

    public static String getAbstract(@NotNull BibtexEntry element) {
        return element.getTagContent("abstract");
    }

    public static String getTagContent(@NotNull BibtexEntry element, String tagName) {
        BibtexEntryContent entryContent = element.getEntryContent();
        if (entryContent == null) return "";

        for (BibtexTag bibtexTag : entryContent.getTagList()) {
            BibtexContent content = bibtexTag.getContent();
            if (tagName.equalsIgnoreCase(bibtexTag.getKey().getText())) {
                String text = BibtexKt.evaluate(content);

                // sanitise double braced strings
                if (text.charAt(0) == '{' && text.charAt(text.length()-1) == '}') {
                    return text.substring(1, text.length() - 1);
                }

                return text;
            }
        }

        return "";
    }
}
