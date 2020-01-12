package nl.hannahsten.texifyidea.psi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.reference.LatexLabelReference;
import nl.hannahsten.texifyidea.settings.TexifySettings;
import nl.hannahsten.texifyidea.util.Magic;
import nl.hannahsten.texifyidea.util.PsiCommandsKt;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is used for method injection in generated parser classes.
 */
public class LatexPsiImplUtil {

    static final Set<String> REFERENCE_COMMANDS = Magic.Command.reference;
    static final Pattern OPTIONAL_SPLIT = Pattern.compile(",");

    @NotNull
    public static PsiReference[] getReferences(@NotNull LatexCommands element) {
        final LatexRequiredParam firstParam = readFirstParam(element);

        if (REFERENCE_COMMANDS.contains(element.getCommandToken().getText()) && firstParam != null) {
            List<PsiReference> references = extractReferences(element, firstParam);
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            return references.toArray(new PsiReference[references.size()]);
        }

        return new PsiReference[0];
    }

    @NotNull
    private static List<PsiReference> extractReferences(@NotNull LatexCommands element, LatexRequiredParam firstParam) {
        List<TextRange> subParamRanges = extractSubParameterRanges(firstParam);

        List<PsiReference> references = new ArrayList<>();
        for (TextRange range : subParamRanges) {
            references.add(new LatexLabelReference(
                    element, range.shiftRight(firstParam.getTextOffset() - element.getTextOffset())
            ));
        }
        return references;
    }

    private static LatexRequiredParam readFirstParam(@NotNull LatexCommands element) {
        return ApplicationManager.getApplication().runReadAction((Computable<LatexRequiredParam>) () -> {
            List<LatexRequiredParam> params = PsiCommandsKt.requiredParameters(element);
            return params.isEmpty() ? null : params.get(0);
        });
    }

    @NotNull
    private static List<TextRange> extractSubParameterRanges(LatexRequiredParam param) {
        return splitToRanges(stripGroup(param.getText()), Magic.Pattern.parameterSplit).stream()
                .map(r -> r.shiftRight(1)).collect(Collectors.toList());
    }

    @NotNull
    private static List<TextRange> splitToRanges(String text, Pattern pattern) {
        String[] parts = pattern.split(text);

        List<TextRange> ranges = new ArrayList<>();

        int currentOffset = 0;
        for (String part : parts) {
            final int partStartOffset = text.indexOf(part, currentOffset);
            ranges.add(TextRange.from(partStartOffset, part.length()));
            currentOffset = partStartOffset + part.length();
        }

        return ranges;
    }

    private static String stripGroup(String text) {
        return text.substring(1, text.length() - 1);
    }

    /**
     * Generates a list of all names of all optional parameters in the command.
     */
    public static List<String> getOptionalParameters(@NotNull LatexCommands element) {
        return element.getParameterList().stream()
                .map(LatexParameter::getOptionalParam)
                .filter(Objects::nonNull)
                .flatMap(op -> {
                    if (op == null || op.getOpenGroup() == null) {
                        return Stream.empty();
                    }

                    return op.getOpenGroup().getContentList().stream()
                            .map(LatexContent::getNoMathContent);
                })
                .filter(Objects::nonNull)
                .map(LatexNoMathContent::getNormalText)
                .filter(Objects::nonNull)
                .map(PsiElement::getText)
                .filter(Objects::nonNull)
                .flatMap(text -> OPTIONAL_SPLIT.splitAsStream(text))
                .collect(Collectors.toList());
    }

    /**
     * Generates a list of all names of all required parameters in the command.
     */
    public static List<String> getRequiredParameters(@NotNull LatexCommands element) {
        return element.getParameterList().stream()
                .map(LatexParameter::getRequiredParam)
                .flatMap(rp -> {
                    if (rp == null || rp.getGroup() == null) {
                        return Stream.empty();
                    }

                    return Stream.of(rp.getGroup());
                })
                .map(group -> {
                    return String.join("", group.getContentList().stream()
                            .flatMap(c -> {
                                LatexNoMathContent content = c.getNoMathContent();

                                if (content == null) {
                                    return Stream.empty();
                                }

                                if (content.getCommands() != null && content.getNormalText() == null) {
                                    return Stream.of(content.getCommands().getCommandToken().getText());
                                }
                                else if (content.getNormalText() != null) {
                                    return Stream.of(content.getNormalText().getText());
                                }

                                return Stream.empty();
                            })
                            .collect(Collectors.toList()));
                })
                .collect(Collectors.toList());
    }

    /**
     * Checks if the command is followed by a label.
     */
    public static boolean hasLabel(@NotNull LatexCommands element) {
        PsiElement grandparent = element.getParent().getParent();
        PsiElement sibling = LatexPsiUtil.getNextSiblingIgnoreWhitespace(grandparent);
        if (sibling == null) {
            return false;
        }

        Collection<LatexCommands> children = PsiTreeUtil.findChildrenOfType(sibling, LatexCommands.class);
        if (children.isEmpty()) {
            return false;
        }

        LatexCommands labelMaybe = children.iterator().next();
        return TexifySettings.getInstance().getLabelPreviousCommands().containsKey(labelMaybe.getCommandToken().getText());
    }
}
