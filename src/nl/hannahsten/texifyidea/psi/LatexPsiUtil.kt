package nl.hannahsten.texifyidea.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.TokenSet
import java.util.*

/**
 * @author Hannah Schellekens
 */
object LatexPsiUtil {

    /**
     * Finds the previous sibling of an element but skips over whitespace.
     *
     * @param element
     * The element to get the previous sibling of.
     * @return The previous sibling of the given psi element, or `null` when there is no
     * previous sibling.
     */
    @JvmStatic
    fun getPreviousSiblingIgnoreWhitespace(element: PsiElement): PsiElement? {
        var sibling: PsiElement? = element
        while (sibling?.prevSibling.also { sibling = it } != null) {
            if (sibling !is PsiWhiteSpace) {
                return sibling
            }
        }
        return null
    }

    /**
     * Finds the next sibling of an element but skips over whitespace.
     *
     * @param element
     * The element to get the next sibling of.
     * @return The next sibling of the given psi element, or `null` when there is no previous
     * sibling.
     */
    @JvmStatic
    fun getNextSiblingIgnoreWhitespace(element: PsiElement): PsiElement? {
        var sibling: PsiElement? = element
        while (sibling?.nextSibling.also { sibling = it } != null) {
            if (sibling !is PsiWhiteSpace) {
                return sibling
            }
        }
        return null
    }

    /**
     * Looks up the first parent of a given child that has the given class.
     *
     * @param child
     * The child from which to find the parent of.
     * @param parentClass
     * The type the parent has.
     * @return The first parent that has the given class, or `null` when the parent can't be
     * found.
     */
    fun <T : PsiElement?> getParentOfType(
        child: PsiElement?,
        parentClass: Class<T>
    ): T? {
        var element = child
        while (element != null) {
            if (parentClass.isAssignableFrom(element.javaClass)) {
                @Suppress("UNCHECKED_CAST")
                return element as T
            }
            element = element.parent
        }
        return element
    }

    /**
     * Get all the elements of the subtree starting at the given Latex [PsiElement].
     *
     *
     * This method uses a depth-first traversal.
     *
     * @param element
     * The [PsiElement] contained in [nl.hannahsten.texifyidea.psi] of which you
     * want to get all the elements of the subtree of.
     * @return A list of all elements in the subtree starting at, and including, the given element
     * at index 0. The list will be empty when the element has no children or when the element is
     * not a Latex element.
     */
    fun getAllChildren(element: PsiElement): List<PsiElement> {
        return getAllChildren(ArrayList(), element)
    }

    /**
     * See [LatexPsiUtil.getAllChildren], but appends all children to the given
     * list.
     */
    private fun getAllChildren(result: MutableList<PsiElement>, element: PsiElement): List<PsiElement> {
        result.add(element)
        for (child in getChildren(element)) {
            getAllChildren(result, child)
        }
        return result
    }

    /**
     * Get all the Latex children of the given Latex [PsiElement]s.
     *
     * @param element
     * The [PsiElement] contained in [nl.hannahsten.texifyidea.psi] of which you
     * want to get all children of.
     * @return A list of all children of the given element. The list will be empty when the element
     * has no children or when the element is not a Latex element.
     */
    fun getChildren(element: PsiElement?): List<PsiElement> {
        val result: MutableList<PsiElement?> = ArrayList()

        // LatexCommands todo check with bnf
        when (element) {
            is LatexCommands -> {
                result.addAll(element.parameterList)
                result.add(element.commandToken)
            }
            is LatexComment -> {
                result.add(element.commentToken)
            }
            is LatexContent -> {
                result.addAll(element.noMathContentList)
            }
            is LatexDisplayMath -> {
                result.add(element.mathContent)
            }
            is LatexGroup -> {
                result.add(element.content)
            }
            is LatexInlineMath -> {
                result.add(element.mathContent)
            }
            is LatexMathEnvironment -> {
                result.add(element.displayMath)
                result.add(element.inlineMath)
            }
            is LatexNoMathContent -> {
                result.add(element.commands)
                result.add(element.comment)
                result.add(element.group)
                //            result.add(noMathContent.getOpenGroup());
                result.add(element.normalText)
            }
            is LatexOptionalParamContent -> {
                result.add(element.group)
                result.add(element.parameterText)
            }
            is LatexRequiredParamContent -> {
                result.add(element.group)
                result.add(element.parameterText)
            }
            is LatexOptionalParam -> {
                result.addAll(element.optionalParamContentList)
            }
            is LatexParameter -> {
                result.add(element.optionalParam)
                result.add(element.requiredParam)
            }
            is LatexRequiredParam -> {
                result.addAll(element.requiredParamContentList)
            }
            is LatexMathContent -> {
                result.addAll(element.noMathContentList)
            }
        }
        return result.filterNotNull()
    }

    /**
     * Returns whether the node has one of the element types specified in the token set.
     */
    fun hasElementType(node: ASTNode, set: TokenSet): Boolean {
        return set.contains(node.elementType)
    }
}