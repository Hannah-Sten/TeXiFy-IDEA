@file:JvmName("MagicComments")

package nl.rubensten.texifyidea.lang.magic

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.psi.LatexGroup
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.lineIndentationByOffset
import nl.rubensten.texifyidea.util.parentOfType

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