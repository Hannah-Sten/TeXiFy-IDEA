package nl.hannahsten.texifyidea.util.parser

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.LatexCommandWithParams
import kotlin.reflect.KClass

/*
 * This file contains utility functions paralleling [Psi] but with improved performance.
 *
 * ## Read Access
 *
 * The methods in this file are assumed to have read access to the PSI tree.
 *
 * Typically, PSI related extensions are called from a read action, so we must avoid wrapping these methods in a read action.
 */

/**
 * Find the first parent (including this element) of this PsiElement that matches the given predicate.
 */
inline fun PsiElement.firstParent(maxDepth: Int = Int.MAX_VALUE, predicate: (PsiElement) -> Boolean): PsiElement? {
    var current: PsiElement = this
    for (depth in 0..maxDepth) {
        if (predicate(current)) {
            return current
        }
        current = current.parent ?: return null
    }
    return null
}

/**
 * Find the first parent excluding this element that matches the given predicate.
 */
inline fun PsiElement.firstStrictParent(maxDepth: Int = Int.MAX_VALUE, predicate: (PsiElement) -> Boolean): PsiElement? {
    return this.parent?.firstParent(maxDepth, predicate)
}

/**
 * Find the first node of the given type in the parent chain (including this) up to the [maxDepth].
 *
 * Usually, you should set [maxDepth] to a reasonable value (1 or 2) to avoid traversing too much into the PSI tree.
 *
 * @param T The type of the element to find.
 * @param maxDepth The maximum depth to search for the element.
 * @see [firstParentOfType]
 */
inline fun <reified T : PsiElement> PsiElement.firstParentOfType(maxDepth: Int = Int.MAX_VALUE): T? {
    return firstParent(maxDepth) { it is T } as? T
}

inline fun PsiElement.traverseParents(action: (PsiElement) -> Unit) {
    var parent: PsiElement? = this.parent
    while (parent != null) {
        action(parent)
        parent = parent.parent
    }
}

/**
 * Determines whether any parent of this PsiElement matches the given predicate.
 */
inline fun PsiElement.anyParent(predicate: (PsiElement) -> Boolean): Boolean {
    traverseParents {
        if (predicate(it)) {
            return true
        }
    }
    return false
}

/**
 * Returns the start offset of this PsiElement in the given ancestor, or -1 if the ancestor is not a parent of this PsiElement.
 */
fun PsiElement.startOffsetInAncestor(ancestor: PsiElement): Int {
    // If the ancestor is not a parent, return -1
    var current: PsiElement? = this
    var startOffset = 0
    while (current != null && current != ancestor) {
        startOffset += current.startOffsetInParent
        current = current.parent
    }
    return if (current == null) -1 else startOffset // ancestor not found in the parent chain
}

/**
 * Returns the text range of this PsiElement in the given ancestor, or null if the ancestor is not a parent of this PsiElement.
 */
fun PsiElement.textRangeInAncestor(ancestor: PsiElement): TextRange? {
    // If the ancestor is not a parent, return null
    val startOffset = this.startOffsetInAncestor(ancestor)
    if (startOffset < 0) return null
    return TextRange.from(startOffset, this.textLength)
}

/**
 * Checks if the psi element is in math mode or not.
 *
 * **This function is slow as it checks all parents of the psi element.**
 *
 * @return `true` when the element is in math mode, `false` when the element is in no math mode.
 */
fun PsiElement.inMathContext(): Boolean {
    traverseParents {
        if (it is LatexMathEnvMarker) return true
        if (it is LatexEnvironment) {
            // TODO: make it possible to check if the environment QUITs math mode
            if (DefaultEnvironment.fromPsi(it)?.context == Environment.Context.MATH) return true
        }
    }
    return false
}

/**
 * Iterate through all direct children of the PsiElement and apply the action to each child
 *
 * @param action The action to apply to each child element. If the action returns false, the iteration stops.
 */
inline fun PsiElement.forEachDirectChild(action: (PsiElement) -> Unit) {
    var child = this.firstChild
    while (child != null) {
        action(child)
        child = child.nextSibling
    }
}

inline fun <reified Psi : PsiElement> PsiElement.forEachDirectChildTyped(action: (Psi) -> Unit) {
    forEachDirectChild {
        if (it is Psi) action(it)
    }
}

inline fun PsiElement.forEachDirectChildReversed(action: (PsiElement) -> Unit) {
    var child = this.lastChild
    while (child != null) {
        action(child)
        child = child.prevSibling
    }
}

/**
 * Apply the given action to each [LatexRequiredParam] in the command.
 *
 * @param action The action to apply to each [LatexRequiredParam] element found in the command. If the action returns false, the traversal stops.
 */
inline fun LatexCommandWithParams.traverseRequiredParams(action: (PsiElement) -> Unit) {
    /*
    Recall the bnf:
    commands ::= COMMAND_TOKEN parameter*
    parameter ::= optional_param | required_param | picture_param | ANGLE_PARAM
     */
    forEachDirectChild { param ->
        param.forEachDirectChild {
            if (it is LatexRequiredParam) {
                action(it)
            }
        }
    }
}

/**
 * Traverse the PSI tree and yield each element (including this element).
 *
 * Consider a tree like
 * ```
 * A
 *  - B
 *    - B1
 *    - B2
 *  - C
 * ```
 * The traversal will be a sequence of `A, B, B1, B2, C`.
 *
 * If you know the PSI structure, then you can set a depth limit to improve performance, especially for large PSI trees.
 *
 * @param depth The maximum depth to traverse the PSI tree.
 * Default is [Int.MAX_VALUE], which means no limit. `depth = 0` means only the current element is traversed.
 */
fun PsiElement.traverse(depth: Int = Int.MAX_VALUE): Sequence<PsiElement> = sequence {
    if (depth < 0) return@sequence
    yield(this@traverse)
    if (depth == 0) return@sequence
    forEachDirectChild { c ->
        yieldAll(c.traverse(depth - 1))
    }
}

private fun PsiElement.traversePruned0(depth: Int = Int.MAX_VALUE, pruneIf: (PsiElement) -> Boolean): Sequence<PsiElement> = sequence {
    val element = this@traversePruned0
    if (depth < 0 || pruneIf(element)) return@sequence
    yield(element)
    if (depth == 0) return@sequence
    forEachDirectChild { c ->
        yieldAll(c.traversePruned0(depth - 1, pruneIf))
    }
}

/**
 * Traverse the PSI tree and yield each element (including this element),
 * but prune the traversal (and the subtree) if the element matches the given predicate.
 * Note that the root element is never pruned.
 *
 *
 * @param depth The maximum depth to traverse the PSI tree.
 * @param pruneIf The predicate to prune the subtree. If it returns true for an element, the whole subtree of that element (inclusive) is not traversed.
 */
fun PsiElement.traversePruneIf(depth: Int = Int.MAX_VALUE, pruneIf: (PsiElement) -> Boolean): Sequence<PsiElement> = sequence {
    if (depth < 0) return@sequence
    yield(this@traversePruneIf)
    if (depth == 0) return@sequence
    forEachDirectChild { c ->
        yieldAll(c.traversePruned0(depth - 1, pruneIf))
    }
}

/**
 * Traverse the PSI tree and yield each element (including this element) in the reversed order.
 *
 * Consider a tree like
 * ```
 * A
 *  - B
 *    - B1
 *    - B2
 *  - C
 * ```
 * The traversal will be a sequence of `A, C, B, B2, B1`.
 *
 * If you know the PSI structure, then you can set a depth limit to improve performance, especially for large PSI trees.
 *
 * @param depth The maximum depth to traverse the PSI tree.
 * Default is [Int.MAX_VALUE], which means no limit. `depth = 0` means only the current element is traversed.
 */
fun PsiElement.traverseReversed(depth: Int = Int.MAX_VALUE): Sequence<PsiElement> = sequence {
    if (depth < 0) return@sequence
    yield(this@traverseReversed)
    if (depth == 0) return@sequence
    forEachDirectChildReversed { c ->
        yieldAll(c.traverse(depth - 1))
    }
}

/**
 * Traverse the PSI tree and yield each element of type [T] (including this element if it is of type [T]).
 *
 * @see traverse
 */
inline fun <reified T : PsiElement> PsiElement.traverseTyped(depth: Int = Int.MAX_VALUE): Sequence<T> {
    return traverse(depth).filterIsInstance<T>()
}

/**
 * Traverse the PSI tree and apply the action to each element (including this element).
 *
 * @param depth The maximum depth to traverse the PSI tree. Default is [Int.MAX_VALUE], which means no limit. `depth = 0` means only the current element is traversed.
 * @param action The action to apply to each element found in the PSI tree.
 */
fun PsiElement.traverse(depth: Int = Int.MAX_VALUE, action: (PsiElement) -> Boolean): Boolean {
    // Traverse the PSI tree and apply the action to each command element
    val visitor = object : MyPsiRecursiveWalker(depth) {
        override fun elementStart(e: PsiElement) {
            if (!action(e)) {
                stopWalking()
            }
        }
    }
    this.accept(visitor)
    return visitor.isWalkingStopped
}

/**
 * Traverse the whole subtree of this [PsiElement] and apply the action to each element (including this element).
 *
 * @see [forEachDirectChild]
 */
inline fun PsiElement.forEachChild(depth: Int = Int.MAX_VALUE, crossinline action: (PsiElement) -> Unit) {
    traverse(depth) {
        action(it)
        true
    }
}

/**
 * Traverse the whole subtree of this [PsiElement] and apply the action to each element of type [T] (including this element if it is of type [T]).
 *
 * @see [forEachDirectChild]
 */
inline fun <reified T : PsiElement> PsiElement.forEachChildTyped(depth: Int = Int.MAX_VALUE, crossinline action: (T) -> Unit) {
    forEachChild(depth) {
        if (it is T) {
            action(it)
        }
    }
}

/**
 * Collects all [PsiElement]s in the subtree of this [PsiElement] that match the given predicate.
 *
 * Note: **This method would be slow as it traverses the entire subtree of the PsiElement.**
 */
fun PsiElement.collectSubtree(predicate: (PsiElement) -> Boolean): List<PsiElement> {
    // Collect all children of the PsiElement that match the predicate
    return PsiTreeUtil.collectElements(this) { element -> predicate(element) }.asList()
}

/**
 * Collects all [PsiElement]s in the subtree of this [PsiElement] that are of type [T].
 *
 * Note: **This method would be slow as it traverses the entire subtree of the PsiElement.**
 */
inline fun <reified T : PsiElement> PsiElement.collectSubtreeTyped(): Collection<T> {
    return PsiTreeUtil.findChildrenOfType(this, T::class.java)
}

/*
Collecting the subtree with a depth limit
 */

/**
 * Collects all [PsiElement]s in the subtree of this [PsiElement] transformed by the given [transform] function into the provided [collection], filtering out `null` values.
 */
fun <R : Any, C : MutableCollection<R>> PsiElement.collectSubtreeTo(collection: C, depth: Int, transform: (PsiElement) -> R?): C {
    // Collect all children of the PsiElement that match the class
    traverse(depth) { element ->
        transform(element)?.let { collection.add(it) }
        true // continue traversing
    }

    return collection
}

/**
 * Collects all [PsiElement]s in the subtree of this [PsiElement] that match the given predicate up to a certain [depth].
 *
 * If you know the PSI structure, then you can set a depth limit to improve performance, especially for large PSI trees.
 *
 */
fun PsiElement.collectSubtree(depth: Int, predicate: (PsiElement) -> Boolean): List<PsiElement> {
    // Collect all children of the PsiElement that match the predicate
    return collectSubtreeTo(mutableListOf(), depth) { it.takeIf(predicate) }
}

/**
 * Collects all [PsiElement]s in the subtree of this [PsiElement] that are of type [T] and match the given predicate up to a certain [depth],
 *
 * If you know the PSI structure, then you can set a depth limit to improve performance, especially for large PSI trees.
 *
 */
inline fun <reified T : PsiElement> PsiElement.collectSubtreeTyped(depth: Int = Int.MAX_VALUE, noinline predicate: (T) -> Boolean): List<T> {
    // Collect all children of the PsiElement that match the predicate
    return collectSubtreeTo(mutableListOf(), depth) {
        if (it is T && predicate(it)) {
            it
        }
        else {
            null
        }
    }
}

/**
 * Collects all [PsiElement]s in the subtree of this [PsiElement] transformed by the given [transform] into a list, filtering out `null` values.
 */
fun <R : Any> PsiElement.collectSubtreeOf(depth: Int = Int.MAX_VALUE, transform: (PsiElement) -> R?): List<R> {
    return collectSubtreeTo(mutableListOf(), depth, transform)
}

/**
 * Finds the first child in the subtree of a certain type.
 *
 * @see findFirstChildOfType
 */
inline fun <reified T : PsiElement> PsiElement.findFirstChildTyped(): T? {
    return PsiTreeUtil.findChildOfType(this, T::class.java)
}

/**
 * Finds the first child in the subtree of a certain type.
 */
fun <T : PsiElement> PsiElement.findFirstChildOfType(clazz: KClass<T>): T? {
    return PsiTreeUtil.findChildOfType(this, clazz.java)
}

/**
 * Finds the first child in the subtree of a certain type that matches the given predicate.
 */
inline fun <reified T : PsiElement> PsiElement.findFirstChildTyped(depth: Int = Int.MAX_VALUE, crossinline predicate: (T) -> Boolean): T? {
    var result: T? = null
    traverse(depth) {
        if (it is T && predicate(it)) {
            result = it
            false
        }
        else {
            true
        }
    }
    return result
}