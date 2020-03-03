package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStub;
import nl.hannahsten.texifyidea.settings.LabelingCommandInformation;
import nl.hannahsten.texifyidea.settings.TexifySettings;
import nl.hannahsten.texifyidea.util.Magic;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class is used for method injection in generated parser classes.
 * It has to be in Java for Grammar-Kit to be able to generate the parser classes correctly.
 */
public class LatexPsiImplUtil {

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

    /**
     * Generates a list of all optional parameter names and values.
     */
    public static LinkedHashMap<String, String> getOptionalParameters(@NotNull LatexCommands element) {
        return LatexCommandsImplUtilKt.getOptionalParameters(element.getParameterList());
    }

    /**
     * Generates a list of all optional parameter names and values.
     */
    public static LinkedHashMap<String, String> getOptionalParameters(@NotNull LatexBeginCommand element) {
        return LatexCommandsImplUtilKt.getOptionalParameters(element.getParameterList());
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
     * Find the label of the environment. The method finds labels inside the environment content as well as labels
     * specified via an optional parameter
     *
     * @return the label name if any, null otherwise
     */
    public static String getLabel(@NotNull LatexEnvironment element) {

        LatexEnvironmentStub stub = element.getStub();
        if (stub != null) return stub.getLabel();

        if (Magic.Environment.labelAsParameter.contains(element.getEnvironmentName())) {
            // see if we can find a label option
            LinkedHashMap<String, String> optionalParameters = LatexCommandsImplUtilKt.getOptionalParameters(element.getBeginCommand().getParameterList());
            return optionalParameters.getOrDefault("label", null);
        }
        else {
            if (!Magic.Environment.labeled.containsKey(element.getEnvironmentName())) return null;

            PsiElement content = element.getEnvironmentContent();
            if (content == null) return null;

            // see if we can find a label command inside the environment
            Collection<LatexCommands> children = PsiTreeUtil.findChildrenOfType(content, LatexCommands.class);
            if (!children.isEmpty()) {
                Map<String, LabelingCommandInformation> labelCommands = TexifySettings.getInstance().getLabelPreviousCommands();
                Optional<LatexCommands> labelCommandOptional = children.stream()
                        .filter(c -> labelCommands.containsKey(c.getName())).findFirst();

                if (!labelCommandOptional.isPresent()) return null;
                LatexCommands labelCommand = labelCommandOptional.get();
                List<String> requiredParameters = labelCommand.getRequiredParameters();
                if (requiredParameters.isEmpty()) return null;
                int parameterPosition = labelCommands.get(labelCommand.getName()).getPosition() - 1;
                if (parameterPosition > requiredParameters.size() - 1 || parameterPosition < 0) return null;
                return requiredParameters.get(parameterPosition);
            }

            return null;
        }
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

    /*
     * LatexNormalText
     */

    public static PsiReference[] getReferences(@NotNull LatexNormalText element) {
        return LatexNormalTextUtilKt.getReferences(element);
    }

    public static PsiReference getReference(@NotNull LatexNormalText element) {
        return LatexNormalTextUtilKt.getReference(element);
    }

    public static PsiElement getNameIdentifier(@NotNull LatexNormalText element) {
        return LatexNormalTextUtilKt.getNameIdentifier(element);
    }

    public static PsiElement setName(@NotNull LatexNormalText element, String name) {
        return LatexNormalTextUtilKt.setName(element, name);
    }

    public static String getName(@NotNull LatexNormalText element) {
        return LatexNormalTextUtilKt.getName(element);
    }
}