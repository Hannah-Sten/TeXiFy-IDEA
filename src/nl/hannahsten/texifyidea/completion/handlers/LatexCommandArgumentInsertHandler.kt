package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import nl.hannahsten.texifyidea.lang.LatexMathCommand
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import java.util.*

/**
 * @author Hannah Schellekens
 */
class LatexCommandArgumentInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        when (val `object` = lookupElement.getObject()) {
            is LatexCommands -> {
                insertCommands(`object`, insertionContext)
            }
            is LatexMathCommand -> {
                insertMathCommand(`object`, insertionContext)
            }
            is LatexRegularCommand -> {
                insertNoMathCommand(`object`, insertionContext)
            }
        }
    }

    private fun insertCommands(commands: LatexCommands, context: InsertionContext) {
        val optional: List<String> = LinkedList(commands.optionalParameters
                .keys)
        if (optional.isEmpty()) return

        var cmdParameterCount = 0
        try {
            cmdParameterCount = optional[0].toInt()
        } catch (ignore: NumberFormatException) {
        }

        if (cmdParameterCount > 0) {
            insert(context, "{}")
        }
    }

    private fun insertMathCommand(mathCommand: LatexMathCommand, context: InsertionContext) {
        if (mathCommand.autoInsertRequired()) {
            insert(context, mathCommand.command)
        }
    }

    private fun insertNoMathCommand(noMathCommand: LatexRegularCommand, context: InsertionContext) {
        if (noMathCommand.autoInsertRequired()) {
            insert(context, noMathCommand.command)
        }
    }

    private fun insert(context: InsertionContext, text: String) {
        val editor = context.editor
        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset

        // When not followed by {}, insert {}.
        if (offset >= document.textLength - 1 ||
                document.getText(TextRange.from(offset, 1)) != "{") {
            insertSquigglyBracketPair(editor, caret)
        }
        else {
            skipSquigglyBrackets(editor, caret)
        }
    }

    private fun skipSquigglyBrackets(editor: Editor, caret: CaretModel) {
        val document = editor.document
        val offset = caret.offset
        var depth = 0
        for (i in offset until editor.document.textLength) {
            when (document.getText(TextRange.from(i, 1))) {
                "{" -> depth++
                "}" -> if (--depth == 0) {
                    caret.moveToOffset(i + 1)
                    return
                }
                else -> {
                }
            }
        }
    }

    private fun insertSquigglyBracketPair(editor: Editor, caret: CaretModel) {
        editor.document.insertString(caret.offset, "{}")
        caret.moveToOffset(caret.offset + 1)
    }
}