package nl.hannahsten.texifyidea.util

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.magic.TextBasedMagicCommentParser
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import kotlin.reflect.KClass

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// PSI ELEMENT ///////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Get the offset where the psi element ends.
 */
fun PsiElement.endOffset(): Int = textOffset + textLength

/**
 * @see [PsiTreeUtil.getChildrenOfType]
 */
fun <T : PsiElement> PsiElement.childrenOfType(clazz: KClass<T>): Collection<T> {
    return PsiTreeUtil.findChildrenOfType(this, clazz.java)
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
            return child as? T
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
    for (child in this.children) {
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
    return hasParent(LatexMathContent::class) || hasParent(LatexDisplayMath::class) || inDirectEnvironmentContext(Environment.Context.MATH)
}

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
 * Check if the element is in a comment or not.
 */
fun PsiElement.inComment() = inDirectEnvironmentContext(Environment.Context.COMMENT) || when (this) {
    is PsiComment -> true
    else -> this is LeafPsiElement && elementType == LatexTypes.COMMAND_TOKEN
}

/**
 * @see LatexPsiUtil.getPreviousSiblingIgnoreWhitespace
 */
fun PsiElement.previousSiblingIgnoreWhitespace() = LatexPsiUtil.getPreviousSiblingIgnoreWhitespace(this)

/**
 * @see LatexPsiUtil.getNextSiblingIgnoreWhitespace
 */
fun PsiElement.nextSiblingIgnoreWhitespace() = LatexPsiUtil.getNextSiblingIgnoreWhitespace(this)

/**
 * Finds the next sibling of the element that has the given type.
 *
 * @return The first following sibling of the given type, or `null` when the sibling couldn't be found.
 */
fun <T : PsiElement> PsiElement.nextSiblingOfType(clazz: KClass<T>): T? {
    var sibling: PsiElement? = this
    while (sibling != null) {
        if (clazz.java.isAssignableFrom(sibling::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return sibling as T
        }

        sibling = sibling.nextSibling
    }

    return null
}

/**
 * Finds the previous sibling of the element that has the given type.
 *
 * @return The first previous sibling of the given type, or `null` when the sibling couldn't be found.
 */
fun <T : PsiElement> PsiElement.previousSiblingOfType(clazz: KClass<T>): T? {
    var sibling: PsiElement? = this
    while (sibling != null) {
        if (clazz.java.isAssignableFrom(sibling::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return sibling as T
        }

        sibling = sibling.prevSibling
    }

    return null
}

/**
 * @see LatexPsiUtil.getAllChildren
 */
fun PsiElement.allChildren(): List<PsiElement> = LatexPsiUtil.getAllChildren(this)

/**
 * @see LatexPsiUtil.getChildren
 */
fun PsiElement.allLatexChildren(): List<PsiElement> = LatexPsiUtil.getChildren(this)

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

/**
 * Checks whether the psi element is part of a comment or not.
 */
fun PsiElement.isComment(): Boolean {
    return this is PsiComment || inDirectEnvironmentContext(Environment.Context.COMMENT)
}

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
    return validNames.contains(nameText.text)
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
 * Performs the given [action] on each child.
 */
inline fun PsiElement.forEachChild(action: (PsiElement) -> Unit) {
    for (child in children) action(child)
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
fun PsiElement.isMagicComment(): Boolean = TextBasedMagicCommentParser.COMMENT_PREFIX.containsMatchIn(text)

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
fun PsiElement.parents(): Sequence<PsiElement> = generateSequence(this) { it.parent }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//// LATEX ELEMENTS ////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Looks up the name of the environment in the required parameter.
 */
fun LatexEnvironment.name(): LatexNormalText? {
    return firstChildOfType(LatexNormalText::class)
}

/**
 * Checks if the environment contains the given context.
 */
fun LatexEnvironment.isContext(context: Environment.Context): Boolean {
    val name = name()?.text ?: return false
    val environment = Environment[name] ?: return false
    return environment.context == context
}

/**
 * Get the environment name of the begin command.
 */
fun LatexBeginCommand.environmentName(): String? = beginOrEndEnvironmentName(this)

/**
 * Finds the [LatexEndCommand] that matches the begin command.
 */
fun LatexBeginCommand.endCommand(): LatexEndCommand? = nextSiblingOfType(LatexEndCommand::class)

/**
 * Looks up all the required parameters from this begin command.
 *
 * @return A list of all required parameters.
 */
fun LatexBeginCommand.requiredParameters(): List<LatexRequiredParam> = parameterList.asSequence()
        .filter { it.requiredParam != null }
        .mapNotNull(LatexParameter::getRequiredParam)
        .toList()

/**
 * Checks if the given latex command marks a valid entry point for latex compilation.
 *
 * A valid entry point means that a latex compilation can start from the file containing the
 * given command.
 *
 * @return `true` if the command marks a valid entry point, `false` if not.
 */
fun LatexBeginCommand.isEntryPoint(): Boolean {
    // Currently: only allowing `\begin{document}`.
    val requiredParameters = requiredParameters()
    return requiredParameters.firstOrNull()?.text == "{document}"
}

/**
 * Get the environment name of the end command.
 */
fun LatexEndCommand.environmentName(): String? = beginOrEndEnvironmentName(this)

/**
 * Get the environment name of a begin/end command.
 *
 * @param element
 *              Either a [LatexBeginCommand] or a [LatexEndCommand]
 */
private fun beginOrEndEnvironmentName(element: PsiElement) = element.firstChildOfType(LatexNormalText::class)?.text

/**
 * Finds the [LatexBeginCommand] that matches the end command.
 */
fun LatexEndCommand.beginCommand(): LatexBeginCommand? = previousSiblingOfType(LatexBeginCommand::class)

/**
 * Checks if the latex content objects is a display math environment.
 */
fun LatexContent.isDisplayMath() = firstChildOfType(LatexDisplayMath::class) != null && firstChildOfType(LatexEnvironment::class) == null

/*
 * Technically it's impossible to determine for all cases whether a users wants to compile with biber or biblatex.
 * But often when people use the biblatex package they use biber.
 * And often, when they use biber they use \printbibliography instead of \bibliography.
 * Hence, the following methods often work - and if they don't, users can easily change the compiler in the run config.
 */

/**
 * Checks if the fileset for this file has a bibliography included.
 *
 * @return `true` when the fileset has a bibliography included, `false` otherwise.
 */
fun PsiFile.hasBibliography() = this.commandsInFileSet().any { it.name == "\\bibliography"}

/**
 * Checks if the fileset for this file uses \printbibliography, in which case the user probably wants to use biber.
 *
 * @return `true` when the fileset has a bibliography included, `false` otherwise.
 */
fun PsiFile.usesBiber() = this.commandsInFileSet().any { it.name == "\\printbibliography"}
