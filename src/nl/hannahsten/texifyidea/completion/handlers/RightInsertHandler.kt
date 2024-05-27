package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.highlighting.BraceMatchingUtil
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexDelimiterCommand

/**
 * Inserts the right part of left-right command pairs, like `\left( \right)`.
 *
 * @author Hannah Schellekens
 */
open class RightInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, element: LookupElement) {
        val editor = context.editor
        val command = element.`object` as? LatexCommand ?: return

        if (command is LatexDelimiterCommand && command.isLeft) {
            val hasMatchingBrace = BraceMatchingUtil.matchBrace(context.editor.document.text, LatexFileType, editor.highlighter.createIterator(context.editor.caretModel.offset - 1), true)
            if (hasMatchingBrace) {
                editor.document.insertString(editor.caretModel.offset, " ")
            } else {
                editor.document.insertString(editor.caretModel.offset, "  \\" + command.matchingName)
            }
            editor.caretModel.moveToOffset(editor.caretModel.offset + 1)
        }
    }
}
