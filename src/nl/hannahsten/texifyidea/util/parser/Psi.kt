package nl.hannahsten.texifyidea.util.parser

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
fun <T : PsiElement> PsiElement.nextSiblingOfType(clazz: KClass<T>): T? = siblingOfType(clazz, PsiElement::getNextSibling)

/**
 * Finds the previous sibling of the element that has the given type.
 *
 * @return The first previous sibling of the given type, or `null` when the sibling couldn't be found.
 */
fun <T : PsiElement> PsiElement.previousSiblingOfType(clazz: KClass<T>): T? = siblingOfType(clazz, PsiElement::getPrevSibling)

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
fun PsiElement.isComment(): Boolean = this is PsiComment

fun PsiElement.isLatexOrBibtex() = language == LatexLanguage || language == BibtexLanguage

/**
 * Checks if the element is one of certain direct environments.
 *
 * This method does not take nested environments into account. Meaning that only the first parent environment counts.
 */
fun PsiElement.inDirectEnvironment(validNames: Set<String>): Boolean {
    val environment = parentOfType(LatexEnvironment::class) ?: return false
    return environment.getEnvironmentName() in validNames
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
fun <Psi : PsiElement> PsiElement.parentsOfType(klass: KClass<out Psi>): Sequence<Psi> = parents().filterIsInstance(klass.java)

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
    override fun accepts(psiElement: PsiElement, context: ProcessingContext?): Boolean = acceptFunction(psiElement, context)
})