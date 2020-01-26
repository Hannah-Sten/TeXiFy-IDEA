package nl.hannahsten.texifyidea.psi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStub;
import nl.hannahsten.texifyidea.reference.InputFileReference;
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
@SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
public class LatexPsiImplUtil {

    static final Set<String> REFERENCE_COMMANDS = Magic.Command.reference;
    static final Set<String> INCLUDE_COMMANDS = Magic.Command.includes;
    static final Set<String> DEFINITION_COMMANDS = Magic.Command.commandDefinitions;
    static final Pattern OPTIONAL_SPLIT = Pattern.compile(",\\s*");

    @NotNull
    public static PsiReference[] getReferences(@NotNull LatexCommands element) {
        final LatexRequiredParam firstParam = readFirstParam(element);

        if (REFERENCE_COMMANDS.contains(element.getCommandToken().getText()) && firstParam != null) {
            List<PsiReference> references = extractReferences(element, firstParam);
            return references.toArray(new PsiReference[references.size()]);
        }

        if (INCLUDE_COMMANDS.contains(element.getCommandToken().getText()) && firstParam != null) {
            List<PsiReference> references = extractIncludes(element, firstParam);
            return references.toArray(new PsiReference[references.size()]);
        }

        List<PsiReference> userDefinedReferences = LatexPsiImplUtilKtKt.userDefinedCommandReferences(element);
        if (userDefinedReferences.size() > 0) {
            return userDefinedReferences.toArray(new PsiReference[userDefinedReferences.size()]);
        }

        return new PsiReference[0];
    }

    @NotNull
    private static List<PsiReference> extractIncludes(@NotNull LatexCommands element, LatexRequiredParam firstParam) {
        List<TextRange> subParamRanges = extractSubParameterRanges(firstParam);

        List<PsiReference> references = new ArrayList<>();
        for (TextRange range : subParamRanges) {
            references.add(new InputFileReference(
                    element, range.shiftRight(firstParam.getTextOffset() - element.getTextOffset())
            ));
        }
        return references;
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
        return getOptionalParameters(element.getParameterList());
    }

    private static List<String> getOptionalParameters(@NotNull List<LatexParameter> parameters) {
        return parameters.stream()
                .map(LatexParameter::getOptionalParam)
                .filter(Objects::nonNull)
                // extract the text of each parameter element
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
                // split the text elements along the comma separator
                .flatMap(text -> OPTIONAL_SPLIT.splitAsStream(text))
                .filter(text -> !text.isEmpty())
                // return only the parameter name for parameters like "param=value"
                .map(text -> text.split("\\s*=\\s*")[0])
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

    /**
     * Checks if the environment contains a label.
     */
    public static boolean hasLabel(@NotNull LatexEnvironment element) {
        PsiElement content = element.getEnvironmentContent();
        if (content == null) {
            return false;
        }

        boolean labelFound = false;

        // see if we can find a label command inside the environment
        Collection<LatexCommands> children = PsiTreeUtil.findChildrenOfType(content, LatexCommands.class);
        if (!children.isEmpty()) {
            LatexCommands labelMaybe = children.iterator().next();
            labelFound = TexifySettings.getInstance().getLabelPreviousCommands().containsKey(labelMaybe.getCommandToken().getText());
        }

        // see if we can find a label option
        List<String> optionalParameters = getOptionalParameters(element.getBeginCommand().getParameterList());
        labelFound = labelFound || optionalParameters.contains("label");

        return labelFound;
    }

    public static String getEnvironmentName(@NotNull LatexEnvironment element) {
        LatexEnvironmentStub stub = element.getStub();
        if (stub != null) return stub.getEnvironmentName();

        List<LatexParameter> parameters = element.getBeginCommand().getParameterList();
        if (parameters.isEmpty()) return "";

        LatexParameter environmentNameParam = parameters.get(0);
        LatexRequiredParam requiredParam = environmentNameParam.getRequiredParam();
        if (requiredParam == null) return "";

        List<LatexContent> contentList = requiredParam.getGroup().getContentList();
        if (contentList.isEmpty()) return "";

        LatexNormalText paramText = contentList.get(0).getNoMathContent().getNormalText();
        if (paramText == null) return "";

        return paramText.getText();
    }
}
