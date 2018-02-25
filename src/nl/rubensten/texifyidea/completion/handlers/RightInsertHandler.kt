package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Editor
import nl.rubensten.texifyidea.lang.LatexMathCommand
import nl.rubensten.texifyidea.util.Magic

/**
 * @author Ruben Schellekens
 */
open class RightInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext?, element: LookupElement?) {
        val editor = context?.editor ?: return
        val command = element?.`object` as? LatexMathCommand ?: return
        val name = command.command

        if (name.startsWith("left")) {
            insertRight(name, editor)
        }
        else if (name == "langle") {
            insertRangle(editor)
        }
    }

    private fun insertRight(commandName: String, editor: Editor) {
        val char = commandName.substring(4)
        val opposite = Magic.Typography.braceOpposites[char] ?: return
        editor.document.insertString(editor.caretModel.offset, "  \\right$opposite")
        editor.caretModel.moveToOffset(editor.caretModel.offset + 1)
    }

    private fun insertRangle(editor: Editor) {
        editor.document.insertString(editor.caretModel.offset, "  \\rangle")
        editor.caretModel.moveToOffset(editor.caretModel.offset + 1)
    }
}
