@file:JvmName("MagicComments")

package nl.rubensten.texifyidea.lang.magic

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentsOfType
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexContent
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.psi.LatexGroup
import nl.rubensten.texifyidea.util.*
import java.util.*

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
        MagicCommentScope.COMMAND -> parentOfType(LatexCommands::class)?.addMagicComment(magicComment)
        MagicCommentScope.GROUP -> parentOfType(LatexGroup::class)?.addMagicComment(magicComment)
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

    val commentLines = LinkedList<String>()

    // Scan (backward) through all the magic comments preceding the element.
    var current: PsiElement? = initial() ?: return MagicComment.empty()

    // Stop searching when a non PsiComment is found or null (the scan ends at the first non-comment).
    while (current is PsiComment) {

        // Only consider magic comments.
        val commentText = current.text
        current = current.next()
        if (TextBasedMagicCommentParser.COMMENT_PREFIX.containsMatchIn(commentText).not()) continue

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
        PsiElement::nextSiblingIgnoreWhitespace,
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
    return forwardMagicCommentLookup { firstChildIgnoringWhitespaceOrNull() }
}

/**
 * Get the magic comment that directly precedes the environment.
 */
fun LatexEnvironment.magicComment(): MagicComment<String, String> {
    val outerContent = parentOfType(LatexContent::class) ?: return MagicComment.empty()
    return outerContent.backwardMagicCommentLookup { previousSiblingIgnoreWhitespace() }
}

/**
 * Get the (merged) magic cbomments that are targetted to this environment, all parent environments and the whole file.
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
 * Get the magic comment that directly targets this command.
 */
fun LatexCommands.magicComment(): MagicComment<String, String> {
    // Because of current parser quirks, there are two scenarios to find the magic comments:
    // 1. When the magic comments are the first elements in an environment, they are next siblings of
    //    the LatexBeginCommand of the parent LatexEnvironment.
    // 2. Otherwise they are previous siblings of the direct LatexContent parent of the LatexCommands.

    val directParentContent = parentOfType(LatexContent::class) ?: return MagicComment.empty()
    val parentContentPreviousSibling = directParentContent.previousSiblingIgnoreWhitespace()

    // Case 2.
    if (parentContentPreviousSibling is PsiComment) {
        return backwardMagicCommentLookup { parentContentPreviousSibling }
    }
    // Case 1.
    else {
        val parentEnvironment = parentOfType(LatexEnvironment::class) ?: return MagicComment.empty()
        val beginCommand = parentEnvironment.firstChildIgnoringWhitespaceOrNull() ?: return MagicComment.empty()
        return forwardMagicCommentLookup { beginCommand.nextSiblingIgnoreWhitespace() }
    }
}

/**
 * Get the (merged) magic cbomments that are targetted to this command, and all parent environments and the whole file.
 */
fun LatexCommands.allMagicComments(): MagicComment<String, String> {
    val result = MutableMagicComment<String, String>()

    // Direct comments.
    result += magicComment()

    /// Parents.
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
 * Get the (merged) magic cbomments that are targetted to this group, all parent goups, commands,
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
}