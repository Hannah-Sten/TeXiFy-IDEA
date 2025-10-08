@file:JvmName("MagicComments")

package nl.hannahsten.texifyidea.lang.magic

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.prevLeaf
import com.intellij.psi.util.prevLeafs
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.lineIndentationByOffset
import nl.hannahsten.texifyidea.util.parser.*

/**
 * Adds the given magic comment as an actual comment to the file.
 */
fun <Key, Value> PsiFile.addMagicComment(magicComment: MagicComment<Key, Value>) {
    val toInsert = magicComment.toCommentString().joinToString("\n") { "%! $it" }
    document()?.insertString(0, "$toInsert\n")
}

/**
 * Adds the given magic comment as an actual comment to this environment.
 */
fun <Key, Value> LatexEnvironment.addMagicComment(magicComment: MagicComment<Key, Value>) {
    addMagicCommentOnPreviousLine(magicComment)
}

/**
 * Adds the given magic comment as an actual comment to this math environment.
 */
fun <Key, Value> LatexMathEnvironment.addMagicComment(magicComment: MagicComment<Key, Value>) {
    addMagicCommentOnPreviousLine(magicComment)
}

/**
 * Adds the given magic comment as an actual comment to this command.
 */
fun <Key, Value> LatexCommands.addMagicComment(magicComment: MagicComment<Key, Value>) {
    addMagicCommentOnPreviousLine(magicComment)
}

/**
 * Adds the given magic comment as an actual comment to this group.
 */
fun <Key, Value> LatexGroup.addMagicComment(magicComment: MagicComment<Key, Value>) {
    val document = containingFile.document() ?: return
    val lines = magicComment.toCommentString()

    // Put the closing brace on a new line.
    val indentation = document.lineIndentationByOffset(textOffset)
    document.replaceString(textOffset + textLength - 1, textOffset + textLength, "\n$indentation}")

    // When it's a one-liner, put it on the opening brace line and put the closing brace on a new line.
    if (lines.size == 1) {
        val toInsert = " %! ${lines.first()}\n$indentation"
        document.insertString(textOffset + 1, toInsert)
    }
    // Otherwise create a block on the new line.
    else {
        val toInsert = lines.joinToString("\n") { "$indentation%! $it" }
        document.insertString(textOffset + 1, "\n$toInsert\n$indentation")
    }
}

/**
 * Adds the given magic comment on the line preceding this psi element, keeping indentation into account.
 */
private fun <Key, Value> PsiElement.addMagicCommentOnPreviousLine(magicComment: MagicComment<Key, Value>) {
    val document = containingFile.document() ?: return
    val indentation = document.lineIndentationByOffset(textOffset)
    val toInsert = magicComment.toCommentString().joinToString("\n$indentation") { "%! $it" }
    document.insertString(textOffset, "$toInsert\n$indentation")
}

/**
 * Adds a magic comment to the given scope of a certain psielement.
 * Does nothing when there is no valid place to put the magic comment.
 */
fun <Key, Value> PsiElement.addMagicComment(magicComment: MagicComment<Key, Value>, scope: MagicCommentScope) {
    when (scope) {
        MagicCommentScope.FILE -> containingFile?.addMagicComment(magicComment)
        MagicCommentScope.ENVIRONMENT -> parentOfType(LatexEnvironment::class)?.addMagicComment(magicComment)
        MagicCommentScope.MATH_ENVIRONMENT -> parentOfType(LatexMathEnvironment::class)?.addMagicComment(magicComment)
        MagicCommentScope.COMMAND -> parentOfType(LatexCommands::class)?.addMagicComment(magicComment)
        MagicCommentScope.GROUP -> parentOfType(LatexGroup::class)?.addMagicComment(magicComment)
    }
}

/**
 * Adds a magic comment to this psi element based on its subtype.
 */
fun <Key, Value> PsiElement.addMagicCommentToPsiElement(magicComment: MagicComment<Key, Value>) {
    when (this) {
        is PsiFile -> this.addMagicComment(magicComment)
        is LatexEnvironment -> this.addMagicComment(magicComment)
        is LatexMathEnvironment -> this.addMagicComment(magicComment)
        is LatexCommands -> this.addMagicComment(magicComment)
        is LatexGroup -> this.addMagicComment(magicComment)
        else -> error("Unsupported PsiElement type ${this.javaClass}")
    }
}

/**
 * Finds all the consecutive (sibling) magic comments relative to a PsiElement.
 * Ignores regular comments.
 *
 * @param initial
 *          Fetches the first candidate comment.
 * @param next
 *          Fetches the next candidate comment.
 * @param reversed
 *          Whether the magic comments must be inserted in reversed order (`true`) or in regular walking order (`false`).
 * @return The parsed magic comment.
 */
fun PsiElement.magicCommentLookup(
    initial: PsiElement.() -> PsiElement?,
    next: PsiElement.() -> PsiElement?,
    reversed: Boolean = false
): MagicComment<String, String> {
    val commentLines = mutableListOf<String>()

    // Scan (backward) through all the magic comments preceding the element.
    var current: PsiElement? = initial() ?: return MagicComment.empty()

    // Stop searching when an element is found that is not a magic comment or that is null
    // (the scan ends at the first element that is not a magic comment).
    while (current.containsMagicComment()) {
        // Only consider magic comments
        val commentText = current?.text ?: continue
        current = current.next()

        // Collect magic comment contents.
        if (reversed) {
            commentLines.addFirst(commentText)
        }
        else commentLines.add(commentText)
    }

    // Parse all collected magic comments.
    val parser = TextBasedMagicCommentParser(commentLines)
    return parser.parse()
}

/**
 * Parses the magic comments that supercede this psi element.
 *
 * @param initial
 *          Fetches the first candidate comment.
 */
fun PsiElement.forwardMagicCommentLookup(initial: PsiElement.() -> PsiElement?) = magicCommentLookup(
    initial,
    PsiElement::nextLeafIgnoreWhitespace,
    reversed = false
)

/**
 * Parses the magic comments that precede this psi element.
 *
 * @param initial
 *          Fetches the first candidate comment.
 */
fun PsiElement.backwardMagicCommentLookup(initial: PsiElement.() -> PsiElement?) = magicCommentLookup(
    initial,
    PsiElement::previousSiblingIgnoreWhitespace,
    reversed = true
)

/**
 * Get the magic comment that directly targets this psi file.
 */
fun PsiFile.magicComment(): MagicComment<String, String> {
    // Find first real element (so the one under LatexContent), which, if it is a magic comment, will be under a LatexNoMathContent
    return forwardMagicCommentLookup { findFirstChildOfType(LatexNoMathContent::class)?.firstChildIgnoringWhitespaceOrNull() }
}

/**
 * Get the magic comment that directly precedes the environment.
 */
fun LatexEnvironment.magicComment(): MagicComment<String, String> {
    val outerContent = parentOfType(LatexNoMathContent::class) ?: return MagicComment.empty()
    return outerContent.backwardMagicCommentLookup { previousSiblingIgnoreWhitespace() }
}

/**
 * Get the (merged) magic comments that are targeted to this environment, all parent environments and the whole file.
 */
fun LatexEnvironment.allMagicComments(): MagicComment<String, String> {
    val result = MutableMagicComment<String, String>()

    // Direct comments.
    result += magicComment()

    // Parents.
    addParentMagicComments(result)

    return result
}

/**
 * Get the magic comment that directly precedes the math environment.
 */
fun LatexMathEnvironment.magicComment(): MagicComment<String, String> {
    // Because of current parser quirks, there are two scenarios to find the magic comments:
    // 1. When the magic comment is the first element in an environment, they are the previous sibling of the
    //    environment content element.
    // 2. Otherwise they are previous siblings of the direct LatexContent parent of the math environment.

    val parentEnvironmentContent = parentOfType(LatexEnvironmentContent::class)
    val parentEnvironmentContentPreviousSibling = parentEnvironmentContent?.previousSiblingIgnoreWhitespace()

    // Case 1.
    if (parentEnvironmentContentPreviousSibling is PsiComment) {
        return parentEnvironmentContent.backwardMagicCommentLookup { previousSiblingIgnoreWhitespace() }
    }

    // Case 2.
    val outerContent = parentOfType(LatexNoMathContent::class) ?: return MagicComment.empty()
    return outerContent.backwardMagicCommentLookup { previousSiblingIgnoreWhitespace() }
}

/**
 * Get the (merged) magic comments that are targeted to this math environment, all parent environments and the whole file.
 */
fun LatexMathEnvironment.allMagicComments(): MagicComment<String, String> {
    val result = MutableMagicComment<String, String>()

    // Direct comments.
    result += magicComment()

    // Parents.
    addParentMagicComments(result)

    return result
}

/**
 * Get the magic comment that directly targets this command.
 */
fun LatexCommands.magicComment(): MagicComment<String, String> {
    val directParentContent = parentOfType(LatexNoMathContent::class) ?: return MagicComment.empty()
    val parentContentPreviousSibling = directParentContent.previousSiblingIgnoreWhitespace()

    if (parentContentPreviousSibling.containsMagicComment()) {
        return backwardMagicCommentLookup { parentContentPreviousSibling }
    }

    return MagicComment.empty()
}

/**
 * Get the (merged) magic comments that are targeted to this command, and all parent environments and the whole file.
 */
fun LatexCommands.allMagicComments(): MagicComment<String, String> {
    val result = MutableMagicComment<String, String>()

    // Direct comments.
    result += magicComment()

    // / Parents.
    addParentMagicComments(result)

    return result
}

/**
 * Get the magic comment that directly targets this group.
 */
fun LatexGroup.magicComment(): MagicComment<String, String> {
    return forwardMagicCommentLookup { firstChildIgnoringWhitespaceOrNull()?.nextSiblingIgnoreWhitespace() }
}

/**
 * Get the (merged) magic comments that are targeted to this group, all parent goups, commands,
 * environments and the whole file.
 */
fun LatexGroup.allMagicComments(): MagicComment<String, String> {
    val result = MutableMagicComment<String, String>()

    // Direct comments.
    result += magicComment()

    // Parents.
    addParentMagicComments(result)

    return result
}

/**
 * Get the direct magic comment of this psi element, or `null` when magic comments are not supported for this element
 * type.
 */
fun PsiElement.magicComment(): MagicComment<String, String>? = when (this) {
    is PsiFile -> this.magicComment()
    is LatexEnvironment -> this.magicComment()
    is LatexMathEnvironment -> this.magicComment()
    is LatexCommands -> this.magicComment()
    is LatexGroup -> this.magicComment()
    else -> this.commentOnPreviousLine()
}

/**
 * To find any comment at the previous line, we need to check for newlines explicitly.
 * Return null if the previous line is not a magic comment.
 */
fun PsiElement.commentOnPreviousLine(): MagicComment<String, String>? {
    return prevLeafs.firstOrNull { it is PsiWhiteSpace && it.text.contains("\n") }?.backwardMagicCommentLookup { prevLeaf(true) }
}

/**
 * Get the (merged) magic comments that are targeted to all the element's parents (including the whole file).
 */
fun PsiElement.allParentMagicComments(): MagicComment<String, String> = MutableMagicComment<String, String>().apply {
    addParentMagicComments(this)
}

/**
 * Adds all the magic comments of the psi element's parents.
 */
private fun PsiElement.addParentMagicComments(result: MutableMagicComment<String, String>) {
    // Direct comments (instead of allMagicComments) are used to prevent duplicate values.

    // Containing file.
    result += containingFile.magicComment()

    // All parent groups.
    parentsOfType<LatexGroup>().filter { it != this }.forEach { group ->
        result += group.magicComment()
    }

    // All parent commands.
    parentsOfType<LatexCommands>().filter { it != this }.forEach { commands ->
        result += commands.magicComment()
    }

    // All parent environments.
    parentsOfType<LatexEnvironment>().filter { it != this }.forEach { environment ->
        result += environment.magicComment()
    }

    // All parent math environments.
    parentsOfType<LatexMathEnvironment>().filter { it != this }.forEach { mathEnvironment ->
        result += mathEnvironment.magicComment()
    }
}

/**
 * Checks if the magic comment has a key `key` which has at least one occurence of the value `value`.
 */
fun MagicComment<String, String>.containsPair(key: String, value: String): Boolean {
    val magicKey = CustomMagicKey(key)
    val magicValues = values(magicKey) ?: return false
    return value in magicValues
}