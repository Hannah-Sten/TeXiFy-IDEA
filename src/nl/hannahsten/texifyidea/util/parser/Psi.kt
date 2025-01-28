package nl.hannahsten.texifyidea.util.parser

import com.intellij.openapi.application.runReadAction
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.nextLeaf
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.grammar.BibtexLanguage
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.magic.TextBasedMagicCommentParser
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import kotlin.reflect.KClass

/**
 * Get the offset where the psi element ends.
 */
fun PsiElement.endOffset(): Int = textOffset + textLength

fun PsiElement.lineNumber(): Int? = containingFile.document()?.getLineNumber(textOffset)

/**
 * @see [PsiTreeUtil.getChildrenOfType]
 */
fun <T : PsiElement> PsiElement.childrenOfType(clazz: KClass<T>): Collection<T> {
    return runReadAction {
        if (!this.isValid || project.isDisposed) {
            emptyList()
        }
        else {
            PsiTreeUtil.findChildrenOfType(this, clazz.java)
        }
    }
}

/**
 * @see [PsiTreeUtil.getChildrenOfType]
 */
inline fun <reified T : PsiElement> PsiElement.childrenOfType(): Collection<T> = childrenOfType(T::class)

/**
 * Finds the first element that matches a given predicate.
 */
@Suppress("UNCHECKED_CAST")
fun <T : PsiElement> PsiElement.findFirstChild(predicate: (PsiElement) -> Boolean): T? {
    for (child in children) {
        if (predicate(this)) {
            return this as? T
        }

        val first = child.findFirstChild<T>(predicate)
        if (first != null) {
            return first
        }
    }

    return null
}

/**
 * Finds the first child of a certain type.
 */
@Suppress("UNCHECKED_CAST")
fun <T : PsiElement> PsiElement.firstChildOfType(clazz: KClass<T>): T? {
    val children = runReadAction { this.children }
    for (child in children) {
        if (clazz.java.isAssignableFrom(child.javaClass)) {
            return child as? T
        }

        val first = child.firstChildOfType(clazz)
        if (first != null) {
            return first
        }
    }

    return null
}

/**
 * Finds the first parent of a certain type.
 */
@Suppress("UNCHECKED_CAST")
fun <T : PsiElement> PsiElement.firstParentOfType(clazz: KClass<T>): T? {
    var current: PsiElement? = this
    while (current != null) {
        if (clazz.java.isAssignableFrom(current.javaClass)) {
            return current as? T
        }
        current = current.parent?.let { if (it.isValid) it else null }
    }
    return null
}

// Kotlin version of the above
inline fun <reified T : PsiElement> PsiElement.firstParentOfType(): T? {
    var current: PsiElement? = this
    while (current != null) {
        if (current is T) {
            return current
        }
        current = current.parent?.let { if (it.isValid) it else null }
    }
    return null
}

/**
 * Finds the last child of a certain type.
 */
@Suppress("UNCHECKED_CAST")
fun <T : PsiElement> PsiElement.lastChildOfType(clazz: KClass<T>): T? {
    for (child in children.reversedArray()) {
        if (clazz.java.isAssignableFrom(child.javaClass)) {
            return child as? T
        }

        val last = child.lastChildOfType(clazz)
        if (last != null) {
            return last
        }
    }

    return null
}

/**
 * @see [PsiTreeUtil.getParentOfType]
 */
fun <T : PsiElement> PsiElement.parentOfType(clazz: KClass<T>): T? = PsiTreeUtil.getParentOfType(this, clazz.java)

/**
 * Checks if the psi element has a parent of a given class.
 */
fun <T : PsiElement> PsiElement.hasParent(clazz: KClass<T>): Boolean = parentOfType(clazz) != null

/**
 * Checks if the psi element is in math mode or not.
 *
 * @return `true` when the element is in math mode, `false` when the element is in no math mode.
 */
fun PsiElement.inMathContext(): Boolean {
    // Do the cheap tests first.
    return inDirectMathContext()
        // Check if any of the parents are in math context, because the direct environment might not be explicitly
        // defined as math context.
        || parents().any { it.inDirectMathContext() }
}

/**
 * Checks if the psi element is in a direct math context or not.
 */
fun PsiElement.inDirectMathContext(): Boolean =
    hasParent(LatexMathContent::class)
        || hasParent(LatexDisplayMath::class)
        || hasParent(LatexMathEnvironment::class)
        || hasParent(LatexInlineMath::class)
        || inDirectEnvironmentContext(Environment.Context.MATH)

/**
 * Returns the outer math environment.
 */
fun PsiElement?.findOuterMathEnvironment(): PsiElement? {
    var element = this
    var outerMathEnvironment: PsiElement? = null

    while (element != null) {
        // get to parent which is *IN* math content
        while (element != null && element.inMathContext().not()) {
            element = element.parent
        }
        // find the marginal element which is NOT IN math content
        while (element != null && element.inMathContext()) {
            element = element.parent
        }

        if (element != null) {
            outerMathEnvironment = when (element.parent) {
                is LatexInlineMath -> element.parent
                is LatexDisplayMath -> element.parent
                else -> element
            }
            element = element.parent
        }
    }
    return outerMathEnvironment
}

/**
 * Checks if the element is inside a verbatim context.
 */
fun PsiElement.inVerbatim() = inDirectEnvironment(EnvironmentMagic.verbatim)

/**
 * Finds the previous sibling of an element but skips over whitespace.
 *
 * @receiver The element to get the previous sibling of.
 * @return The previous sibling of the given psi element, or `null` when there is no
 * previous sibling.
 */
fun PsiElement.previousSiblingIgnoreWhitespace(): PsiElement? {
    var sibling: PsiElement? = this
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
 * @receiver The element to get the next sibling of.
 * @return The next sibling of the given psi element, or `null` when there is no previous
 * sibling.
 */
fun PsiElement.nextSiblingIgnoreWhitespace(): PsiElement? {
    var sibling: PsiElement? = this
    while (sibling?.nextSibling.also { sibling = it } != null) {
        if (sibling !is PsiWhiteSpace) {
            return sibling
        }
    }
    return null
}

/**
 * Finds the next leaf element (which is not necessarily a sibling) but skips over whitespace.
 *
 * @receiver The element to get the next sibling of.
 * @return The next sibling of the given psi element, or `null` when there is no previous
 * sibling.
 */
fun PsiElement.nextLeafIgnoreWhitespace(): PsiElement? {
    var leaf: PsiElement? = this
    while (leaf?.nextLeaf(true).also { leaf = it } != null) {
        if (leaf !is PsiWhiteSpace) {
            return leaf
        }
    }
    return null
}

/**
 * Finds the next sibling of the element that has the given type.
 * If the element has the given type, it is returned directly.
 *
 * @return The first following sibling of the given type, or `null` when the sibling couldn't be found.
 */
fun <T : PsiElement> PsiElement.nextSiblingOfType(clazz: KClass<T>): T? {
    return siblingOfType(clazz, PsiElement::getNextSibling)
}

/**
 * Finds the previous sibling of the element that has the given type.
 *
 * @return The first previous sibling of the given type, or `null` when the sibling couldn't be found.
 */
fun <T : PsiElement> PsiElement.previousSiblingOfType(clazz: KClass<T>): T? {
    return siblingOfType(clazz, PsiElement::getPrevSibling)
}

private fun <T : PsiElement> PsiElement.siblingOfType(clazz: KClass<T>, next: PsiElement.() -> PsiElement): T? {
    var sibling: PsiElement? = this
    while (sibling != null) {
        if (clazz.java.isAssignableFrom(sibling::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return sibling as T
        }

        sibling = sibling.next()
    }

    return null
}

/**
 * Finds the `generations`th parent of the psi element.
 */
fun PsiElement.grandparent(generations: Int): PsiElement? {
    var parent: PsiElement = this
    for (i in 1..generations) {
        parent = parent.parent ?: return null
    }
    return parent
}

/**
 * Checks if the psi element has a (grand) parent that matches the given predicate.
 */
inline fun PsiElement.hasParentMatching(maxDepth: Int, predicate: (PsiElement) -> Boolean): Boolean {
    var count = 0
    var parent = this.parent
    while (parent != null && parent !is PsiFile) {
        if (predicate(parent)) {
            return true
        }

        parent = parent.parent

        if (count++ > maxDepth) {
            return false
        }
    }

    return false
}

val commandTokens = setOf(LatexTypes.COMMAND_TOKEN, LatexTypes.LEFT, LatexTypes.RIGHT)

/**
 * Checks whether the psi element is part of a comment or not.
 */
fun PsiElement.isComment(): Boolean {
    return this is PsiComment || inDirectEnvironmentContext(Environment.Context.COMMENT)
}

fun PsiElement.isLatexOrBibtex() = language == LatexLanguage || language == BibtexLanguage

/**
 * Checks if the element is in a direct environment.
 *
 * This method does not take nested environments into account. Meaning that only the first parent environment counts.
 */
fun PsiElement.inDirectEnvironment(environmentName: String): Boolean = inDirectEnvironment(listOf(environmentName))

/**
 * Checks if the element is one of certain direct environments.
 *
 * This method does not take nested environments into account. Meaning that only the first parent environment counts.
 */
fun PsiElement.inDirectEnvironment(validNames: Collection<String>): Boolean {
    val environment = parentOfType(LatexEnvironment::class) ?: return false
    val nameText = environment.name() ?: return false
    return nameText.text in validNames
}

/**
 * Runs the given predicate on the direct environment element of this psi element.
 *
 * @return `true` when the predicate tests `true`, or `false` when there is no direct environment or when the
 *              predicate failed.
 */
inline fun PsiElement.inDirectEnvironmentMatching(predicate: (LatexEnvironment) -> Boolean): Boolean {
    val environment = parentOfType(LatexEnvironment::class) ?: return false
    return predicate(environment)
}

/**
 * Checks if the psi element is a child of `parent`.
 *
 * @return `true` when the element is a child of `parent`, `false` when the element is not a child of `parent` or when
 *          `parent` is `null`
 */
fun PsiElement.isChildOf(parent: PsiElement?): Boolean {
    if (parent == null) {
        return false
    }

    return hasParentMatching(1000) { it == parent }
}

/**
 * Checks if the psi element has a direct environment with the given context.
 */
fun PsiElement.inDirectEnvironmentContext(context: Environment.Context): Boolean {
    val environment = parentOfType(LatexEnvironment::class) ?: return context == Environment.Context.NORMAL
    return inDirectEnvironmentMatching {
        DefaultEnvironment.fromPsi(environment)?.context == context
    }
}

/**
 * Performs the given [action] on each child, in order.
 */
inline fun PsiElement.forEachChild(action: (PsiElement) -> Unit) {
    for (child in children) action(child)
}

/**
 * Performs the given [action] on each child of the given type `Psi`, in order.
 */
inline fun <reified Psi : PsiElement> PsiElement.forEachChildOfType(action: (PsiElement) -> Unit) = forEachChild {
    if (it is Psi) {
        action(it)
    }
}

/**
 * Finds the `n`th (index) child of the given type.
 */
inline fun <reified ChildPsi : PsiElement> PsiElement.nthChildOfType(index: Int): ChildPsi? {
    var pointer = 0
    forEachChildOfType<ChildPsi> {
        if (pointer == index) {
            return it as ChildPsi
        }
        pointer++
    }
    return null
}

/**
 * Finds the first child of the PsiElement that is not whitespace.
 * When there is no first child that is not whitespace, this function returns `null`.
 */
fun PsiElement.firstChildIgnoringWhitespaceOrNull(): PsiElement? {
    var child: PsiElement? = firstChild ?: return null
    while (child is PsiWhiteSpace) {
        child = child.nextSiblingIgnoreWhitespace()
    }
    return child
}

/**
 * Checks if the PsiComment is actually a Magic Comment.
 *
 * @return `true` if it is a magic comment, `false` otherwise.
 */
fun PsiElement?.containsMagicComment(): Boolean =
    this?.text?.let { t -> TextBasedMagicCommentParser.COMMENT_PREFIX.containsMatchIn(t) } ?: false

/**
 * Remove a psi element from the psi tree.
 *
 * Also remove the white space in front of the psi element when [removeLeadingWhiteSpace] is true, so we don't end up
 * with ridiculous amounts of white space.
 */
fun PsiElement.remove(removeLeadingWhiteSpace: Boolean = true) {
    if (removeLeadingWhiteSpace) {
        previousSiblingOfType(PsiWhiteSpace::class)?.let {
            it.parent.node.removeChild(it.node)
        }
    }
    parent.node.removeChild(node)
}

/**
 * Get a sequence of all the parents of this PsiElement with the given type.
 */
inline fun <reified Psi : PsiElement> PsiElement.parentsOfType(): Sequence<Psi> = parentsOfType(Psi::class)

/**
 * Get a sequence of all parents of this PsiElement that are of the given type.
 */
fun <Psi : PsiElement> PsiElement.parentsOfType(klass: KClass<out Psi>): Sequence<Psi> {
    return parents().filterIsInstance(klass.java)
}

/**
 * Get a sequence of all parents of this element.
 */
fun PsiElement.parents(): Sequence<PsiElement> = generateSequence(this) {
    it.parent?.run { if (!isValid) null else this }
}

/**
 * Adds the pattern condition to this psi element pattern.
 */
fun <Psi : PsiElement> PsiElementPattern.Capture<Psi>.withPattern(
    debugName: String? = null,
    acceptFunction: (psiElement: PsiElement, context: ProcessingContext?) -> Boolean
) = with(object : PatternCondition<PsiElement>(debugName) {

    // This helper function allows for a simple lambda on  the call site.
    override fun accepts(psiElement: PsiElement, context: ProcessingContext?): Boolean {
        return acceptFunction(psiElement, context)
    }
})

/**
 * Looks for the index of this child element in the children of the parent that share the type with this child.
 * Zero indexed.
 *
 * Example:
 * PARENT
 * - CHILD A
 * - CHILD B
 * - BREAD A
 * - BREAD B
 * - CHILD C
 *
 * `CHILD C.indexOfChildByType(PARENT) = 2`.
 *
 * @receiver The parent of the children to get the index of.
 * @return The index of this element in the child list, ignoring children of a different type, or `null` when the parent
 *          could not be found, or when no child could be found that matches `this`.
 */
inline fun <reified PsiChild : PsiElement, reified PsiParent : PsiElement> PsiChild.indexOfChildByType(): Int? {
    val parentElement = parentOfType(PsiParent::class) ?: return null

    // Loop over all children to find this parameter.
    var currentIndex = 0
    parentElement.forEachChildOfType<PsiChild> {
        if (it == this) {
            return currentIndex
        }
        currentIndex++
    }

    // No child found.
    return null
}