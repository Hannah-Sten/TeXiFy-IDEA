package nl.hannahsten.texifyidea.util.parser

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.psi.*

/*
 * This file contains utility functions paralleling [Psi] but with improved performance.
 *
 * ## Read Access
 *
 * The methods in this file are assumed read access to the PSI tree.
 *
 * Typically, PSI related extensions are called from a read action, so we must avoid wrapping these methods in a read action.
 */

/**
 * Determines whether any parent of this PsiElement matches the given predicate.
 */
inline fun PsiElement.anyParent(predicate: (PsiElement) -> Boolean): Boolean {
    var parent: PsiElement? = this.parent
    while (parent != null) {
        if (predicate(parent)) {
            return true
        }
        parent = parent.parent
    }
    return false
}

/**
 * Checks if the psi element is in math mode or not.
 *
 * **This function is slow as it checks all parents of the psi element.**
 *
 * @return `true` when the element is in math mode, `false` when the element is in no math mode.
 */
fun PsiElement.inMathContext(): Boolean {
    // TODO: performance
    // 1st improved version, test if any parent element is in a math environment
    return anyParent { it is LatexMathEnvMarker }
        || anyParent { it.inDirectEnvironmentContext(Environment.Context.MATH) } // can be improved
}

/**
 * Traverse the PSI tree and apply the action to each command element.
 *
 * @param action The action to apply to each [LatexCommands] element found in the PSI tree.
 */
fun PsiElement.traverseCommands(action: (LatexCommands) -> Unit) {
    // Traverse the PSI tree and apply the action to each command element
    this.accept(LatexCommandTraverser(action))
}

/**
 * Traverse the PSI tree and apply the action to each command element.
 *
 * @param action The action to apply to each [LatexCommands] element found in the PSI tree.
 */
fun PsiElement.traverse(action: (LatexCommands) -> Boolean): Boolean {
    // Traverse the PSI tree and apply the action to each command element
    val visitor = LatexCommandTraverserStoppable(action)
    this.accept(visitor)
    return visitor.traversalStopped
}

/**
 * Collects all [PsiElement]s in the subtree of this [PsiElement] that match the given predicate.
 *
 * **This method would be slow as it traverses the entire subtree of the PsiElement.**
 */
fun PsiElement.collectSubtree(predicate: (PsiElement) -> Boolean): List<PsiElement> {
    // Collect all children of the PsiElement that match the predicate
    return PsiTreeUtil.collectElements(this) { element -> predicate(element) }.asList()
}

/**
 * Iterate through all direct children of the PsiElement and apply the action to each child
 *
 * @param action The action to apply to each child element. If the action returns false, the iteration stops.
 */
inline fun PsiElement.forEachDirectChild(action: (PsiElement) -> Boolean): Boolean {
    for (child in this.children) {
        if (!action(child)) return false
    }
    return true
}

/**
 * Apply the given action to each [LatexRequiredParam] in the command.
 *
 * @param action The action to apply to each [LatexRequiredParam] element found in the command. If the action returns false, the traversal stops.
 */
fun LatexCommandWithParams.traverseRequiredParams(action: (PsiElement) -> Boolean) {
    /*
    Recall the bnf:
    commands ::= COMMAND_TOKEN parameter*
    parameter ::= optional_param | required_param | picture_param | ANGLE_PARAM
     */
    forEachDirectChild { param ->
        param.forEachDirectChild {
            if (it is LatexRequiredParam) {
                val res = action(it)
                if (!res) return@forEachDirectChild false
            }
            true
        }
    }
}

/**
 * Gets the contextual surrounder of the current [PsiElement].
 *
 * See the example below for the structure of the PSI tree, the contextual surrounder is the `no_math_content` elements or `PsiWhiteSpace`
 * ```
 * - root
 *   - no_math_content
 *     - element
 *   - PsiWhiteSpace
 *   - no_math_content
 *     - element
 * ```
 */
fun PsiElement.contextualSurrounder(): PsiElement? {
    // Find the contextual element of the current element in the PSI tree.
    // The current element is surrounded by `no_math_content`, so we should go an extra level up
    return when (this) {
        is PsiWhiteSpace
        -> this
        else -> parent as? LatexNoMathContent
    }
}

/**
 * This is a template function for traversing the contextual siblings of the current [PsiElement].
 * Use [traverseContextualSiblingsNext] or [traverseContextualSiblingsPrev] instead.
 */
inline fun PsiElement.traverseContextualSiblingsTemplate(action: (PsiElement) -> Unit, siblingFunc: (PsiElement) -> PsiElement?) {
    val parent = contextualSurrounder() ?: return

    var siblingContent = siblingFunc(parent)
    while (siblingContent != null) {
        if (siblingContent is LatexNoMathContent) {
            val sibling = siblingContent.firstChild ?: continue
            action(sibling)
        }
        siblingContent = siblingFunc(siblingContent)
    }
}

/**
 * Gets the contextual siblings of the current [LatexComposite] element.
 *
 * See the example below for the structure of the PSI tree:
 *
 *     some root
 *       - no_math_content
 *         - previous sibling
 *       - no_math_content
 *         - the current element
 *       - no_math_content
 *         - next sibling
 *
 *
 *
 *  @see [traverseContextualSiblingsNext]
 *  @see [traverseContextualSiblingsPrev]
 */
fun <T : Any> PsiElement.contextualSiblings(): List<LatexComposite> {
    // Find the contextual siblings of the current element in the PSI tree.
    // The current element is surrounded by `no_math_content`, so we should go an extra level up
    val parent = contextualSurrounder() ?: return emptyList()
    val grandParent = parent.parent ?: return emptyList()
    val result = mutableListOf<LatexComposite>()
    grandParent.forEachDirectChild { siblingParent ->
        if (siblingParent is LatexNoMathContent) {
            siblingParent.firstChild?.let { child ->
                if (child is LatexComposite && child != this) {
                    result.add(child)
                }
            }
        }
        true // continue iterating
    }
    return result
}

/**
 * Traverse the contextual siblings of the current [LatexComposite] element that are after it in the PSI tree in order.
 *
 * You can return non-locally from the [action] to stop the traversal.
 *
 * For the structure of the PSI tree, see the example in [contextualSiblings].
 *
 * @see [contextualSiblings]
 */
inline fun PsiElement.traverseContextualSiblingsNext(action: (PsiElement) -> Unit) {
    return traverseContextualSiblingsTemplate(action) { it.nextSibling }
}

/**
 * Traverse the contextual siblings of the current [LatexComposite] element that are before it in the PSI tree in order.
 *
 * You can return non-locally in from the [action] to stop the traversal.
 *
 * For the structure of the PSI tree, see the example in [contextualSiblings].
 *
 * @see [contextualSiblings]
 *
 */
inline fun PsiElement.traverseContextualSiblingsPrev(action: (PsiElement) -> Unit) {
    return traverseContextualSiblingsTemplate(action) { it.prevSibling }
}