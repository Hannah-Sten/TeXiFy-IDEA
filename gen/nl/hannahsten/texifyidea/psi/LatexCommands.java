// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface LatexCommands extends StubBasedPsiElement<LatexCommandsStub>, PsiNamedElement {

    static final Pattern OPTIONAL_SPLIT = Pattern.compile(",");

    @NotNull
    List<LatexParameter> getParameterList();

    @NotNull
    PsiElement getCommandToken();

    default String getName() {
        return getCommandToken().getText();
    }

    /**
     * Generates a list of all names of all optional parameters in the command.
     */
    default List<String> getOptionalParameters() {
        return getParameterList().stream()
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
    default List<String> getRequiredParameters() {
        return getParameterList().stream()
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
    default boolean hasLabel() {
        PsiElement grandparent = getParent().getParent();
        PsiElement sibling = LatexPsiUtil.getNextSiblingIgnoreWhitespace(grandparent);
        if (sibling == null) {
            return false;
        }

        Collection<LatexCommands> children = PsiTreeUtil.findChildrenOfType(sibling, LatexCommands.class);
        if (children.isEmpty()) {
            return false;
        }

        LatexCommands labelMaybe = children.iterator().next();
        return "\\label".equals(labelMaybe.getName());
    }
}
