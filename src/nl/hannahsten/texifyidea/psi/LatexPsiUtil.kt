package nl.hannahsten.texifyidea.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hannah Schellekens
 */
public class LatexPsiUtil {

    private LatexPsiUtil() {
    }

    /**
     * Finds the previous sibling of an element but skips over whitespace.
     *
     * @param element
     *         The element to get the previous sibling of.
     * @return The previous sibling of the given psi element, or {@code null} when there is no
     * previous sibling.
     */
    @Nullable
    public static PsiElement getPreviousSiblingIgnoreWhitespace(@NotNull PsiElement element) {
        PsiElement sibling = element;
        while ((sibling = sibling.getPrevSibling()) != null) {
            if (!(sibling instanceof PsiWhiteSpace)) {
                return sibling;
            }
        }

        return null;
    }

    /**
     * Finds the next sibling of an element but skips over whitespace.
     *
     * @param element
     *         The element to get the next sibling of.
     * @return The next sibling of the given psi element, or {@code null} when there is no previous
     * sibling.
     */
    @Nullable
    public static PsiElement getNextSiblingIgnoreWhitespace(@NotNull PsiElement element) {
        PsiElement sibling = element;
        while ((sibling = sibling.getNextSibling()) != null) {
            if (!(sibling instanceof PsiWhiteSpace)) {
                return sibling;
            }
        }

        return null;
    }

    /**
     * Looks up the first parent of a given child that has the given class.
     *
     * @param child
     *         The child from which to find the parent of.
     * @param parentClass
     *         The type the parent has.
     * @return The first parent that has the given class, or {@code null} when the parent can't be
     * found.
     */
    @Nullable
    public static <T extends PsiElement> T getParentOfType(@Nullable PsiElement child,
                                                           @NotNull Class<T> parentClass) {
        PsiElement element = child;
        while (element != null) {
            if (parentClass.isAssignableFrom(element.getClass())) {
                return (T)element;
            }

            element = element.getParent();
        }

        return (T)element;
    }

    /**
     * Get all the elements of the subtree starting at the given Latex {@link PsiElement}.
     * <p>
     * This method uses a depth-first traversal.
     *
     * @param element
     *         The {@link PsiElement} contained in {@link nl.hannahsten.texifyidea.psi} of which you
     *         want to get all the elements of the subtree of.
     * @return A list of all elements in the subtree starting at, and including, the given element
     * at index 0. The list will be empty when the element has no children or when the element is
     * not a Latex element.
     */
    public static List<PsiElement> getAllChildren(PsiElement element) {
        return getAllChildren(new ArrayList<>(), element);
    }

    /**
     * See {@link LatexPsiUtil#getAllChildren(PsiElement)}, but appends all children to the given
     * list.
     */
    private static List<PsiElement> getAllChildren(List<PsiElement> result, PsiElement element) {
        result.add(element);

        for (PsiElement child : getChildren(element)) {
            getAllChildren(result, child);
        }

        return result;
    }

    /**
     * Get all the Latex children of the given Latex {@link PsiElement}s.
     *
     * @param element
     *         The {@link PsiElement} contained in {@link nl.hannahsten.texifyidea.psi} of which you
     *         want to get all children of.
     * @return A list of all children of the given element. The list will be empty when the element
     * has no children or when the element is not a Latex element.
     */
    public static List<PsiElement> getChildren(PsiElement element) {
        List<PsiElement> result = new ArrayList<>();

        // LatexCommands
        if (element instanceof LatexCommands) {
            LatexCommands commands = (LatexCommands)element;
            result.addAll(commands.getParameterList());
            result.add(commands.getCommandToken());
        }
        // LatexComment
        else if (element instanceof LatexComment) {
            LatexComment comment = (LatexComment)element;
            result.add(comment.getCommentToken());
        }
        // LatexContent
        else if (element instanceof LatexContent) {
            LatexContent content = (LatexContent)element;
            result.add(content.getNoMathContent());
        }
        // LatexDisplayMath
        else if (element instanceof LatexDisplayMath) {
            LatexDisplayMath displayMath = (LatexDisplayMath)element;
            result.add(displayMath.getMathContent());
        }
        // LatexGroup
        else if (element instanceof LatexGroup) {
            LatexGroup group = (LatexGroup)element;
            result.addAll(group.getContentList());
        }
        // LatexInlineMath
        else if (element instanceof LatexInlineMath) {
            LatexInlineMath inlineMath = (LatexInlineMath)element;
            result.add(inlineMath.getMathContent());
        }
        // LatexMathEnvironment
        else if (element instanceof LatexMathEnvironment) {
            LatexMathEnvironment mathEnvironment = (LatexMathEnvironment)element;
            result.add(mathEnvironment.getDisplayMath());
            result.add(mathEnvironment.getInlineMath());
        }
        // LatexNoMathContent
        else if (element instanceof LatexNoMathContent) {
            LatexNoMathContent noMathContent = (LatexNoMathContent)element;
            result.add(noMathContent.getCommands());
            result.add(noMathContent.getComment());
            result.add(noMathContent.getGroup());
//            result.add(noMathContent.getOpenGroup());
            result.add(noMathContent.getNormalText());
        }
        // LatexOptionalParamContent
        else if (element instanceof LatexOptionalParamContent) {
            LatexOptionalParamContent paramContent = (LatexOptionalParamContent)element;
            result.add(paramContent.getGroup());
            result.add(paramContent.getNormalText());
        }
        // LatexOptionalParam
        else if (element instanceof LatexOptionalParam) {
            LatexOptionalParam optionalParam = (LatexOptionalParam)element;
            result.addAll(optionalParam.getOptionalParamContentList());
        }
        // LatexParameter
        else if (element instanceof LatexParameter) {
            LatexParameter parameter = (LatexParameter)element;
            result.add(parameter.getOptionalParam());
            result.add(parameter.getRequiredParam());
        }
        // LatexRequiredParam
        else if (element instanceof LatexRequiredParam) {
            LatexRequiredParam requiredParam = (LatexRequiredParam)element;
            result.add(requiredParam.getGroup());
        }
        // LatexMathContent
        else if (element instanceof LatexMathContent) {
            LatexMathContent mathContent = (LatexMathContent)element;
            result.addAll(mathContent.getNoMathContentList());
        }

        return result;
    }

    /**
     * Returns whether the node has one of the element types specified in the token set.
     */
    public static boolean hasElementType(@NotNull ASTNode node, @NotNull TokenSet set) {
        return set.contains(node.getElementType());
    }
}
