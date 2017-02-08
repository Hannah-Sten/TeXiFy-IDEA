package nl.rubensten.texifyidea.psi;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruben Schellekens
 */
public class LatexPsiUtil {

    private LatexPsiUtil() {
    }

    /**
     * Get all the Latex children of the given Latex {@link PsiElement}s.
     *
     * @param element
     *         The {@link PsiElement} contained in {@link nl.rubensten.texifyidea.psi} of which you
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
            result.add(content.getMathEnvironment());
            result.add(content.getNoMathContent());
        }
        // LatexDisplayMath
        else if (element instanceof LatexDisplayMath) {
            LatexDisplayMath displayMath = (LatexDisplayMath)element;
            result.addAll(displayMath.getNoMathContentList());
        }
        // LatexGroup
        else if (element instanceof LatexGroup) {
            LatexGroup group = (LatexGroup)element;
            result.addAll(group.getContentList());
        }
        // LatexInlineMath
        else if (element instanceof LatexInlineMath) {
            LatexInlineMath inlineMath = (LatexInlineMath)element;
            result.addAll(inlineMath.getNoMathContentList());
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
            result.add(noMathContent.getOpenGroup());
            result.add(noMathContent.getNormalText());
        }
        // LatexOpenGroup
        else if (element instanceof LatexOpenGroup) {
            LatexOpenGroup openGroup = (LatexOpenGroup)element;
            result.addAll(openGroup.getContentList());
        }
        // LatexOptionalParam
        else if (element instanceof LatexOptionalParam) {
            LatexOptionalParam optionalParam = (LatexOptionalParam)element;
            result.add(optionalParam.getOpenGroup());
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

        return result;
    }

}