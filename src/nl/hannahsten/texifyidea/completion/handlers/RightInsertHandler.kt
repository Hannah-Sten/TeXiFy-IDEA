package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Editor
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.util.Magic

/**
 * Inserts the right part of left-right command pairs, like `\left( \right)`.
 *
 * @author Hannah Schellekens
 */
open class RightInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, element: LookupElement) {
        val editor = context.editor
        val command = element.`object` as? LatexCommand ?: return
        val name = command.command

        if (name.startsWith("left")) {
            insertRightBraceCommand(name, editor)
        }
        else if (name == "langle") {
            insertRightCommand(editor, "\\rangle", spacing = " ")
        }
        else if (name == "lq") {
            insertRightCommand(editor, "\\rq", spacing = "", suffix = "{}")
        }
    }

    private fun insertRightBraceCommand(commandName: String, editor: Editor) {
        val char = commandName.substring(4)
        val opposite = Magic.Typography.braceOpposites[char] ?: return
        editor.document.insertString(editor.caretModel.offset, "  \\right$opposite")
        editor.caretModel.moveToOffset(editor.caretModel.offset + 1)
    }

    private fun insertRightCommand(editor: Editor, rightCommand: String, spacing: String = "", suffix: String = "") {
        editor.document.insertString(editor.caretModel.offset, "$suffix$spacing$spacing$rightCommand$suffix")
        editor.caretModel.moveToOffset(editor.caretModel.offset + spacing.length + suffix.length)
    }
}
