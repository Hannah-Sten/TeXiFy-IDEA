package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import nl.hannahsten.texifyidea.lang.Argument
import nl.hannahsten.texifyidea.lang.LatexMathCommand
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredArgument
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
class LatexCommandArgumentInsertHandler(val arguments: List<Argument>? = null) : InsertHandler<LookupElement> {
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        removeWhiteSpaces(insertionContext)

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
        val optional: List<String> = commands.optionalParameters.keys.toList()
        if (optional.isEmpty()) return

        var cmdParameterCount = 0
        try {
            cmdParameterCount = optional[0].toInt()
        }
        catch (ignore: NumberFormatException) {
        }

        if (cmdParameterCount > 0) {
            insert(context, cmdParameterCount)
        }
    }

    private fun insertMathCommand(mathCommand: LatexMathCommand, context: InsertionContext) {
        if (mathCommand.autoInsertRequired()) {
            insert(context, mathCommand.arguments
                .count { it is RequiredArgument })
        }
    }

    private fun insertNoMathCommand(noMathCommand: LatexRegularCommand, context: InsertionContext) {
        if (noMathCommand.autoInsertRequired()) {
            insert(context, noMathCommand.arguments
                .count { it is RequiredArgument })
        }
    }

    private fun insert(context: InsertionContext, numberOfBracesPairs: Int) {
        val editor = context.editor
        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset
        // When not followed by { or [ (whichever the first parameter starts with) insert the parameters.
        if (arguments == null || offset >= document.textLength - 1 || document.getText(
                TextRange.from(
                    offset,
                    1
                )
            ) != "{"
        ) {
            insertParametersLiveTemplate(editor)
        }
        else {
            skipParameters()
        }
    }

    private fun insertParametersLiveTemplate(editor: Editor) {
        // arguments is not null, we checked when calling this function.
        val template = TemplateImpl(
            "",
            arguments!!.mapIndexed { index: Int, argument: Argument ->
                if (argument is RequiredArgument) "{\$__Variable$index\$}" else "[\$__Variable$index\$]"
            }.joinToString(""),
            ""
        )
        repeat(arguments.size) { template.addVariable(TextExpression(""), true) }
        TemplateManager.getInstance(editor.project).startTemplate(editor, template)
    }

    private fun skipParameters() {
        // TODO implement skipSquigglyBrackets, but for skipping over all parameters.
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

    /**
     * Remove whitespaces that are inserted by the lookup text...
     */
    private fun removeWhiteSpaces(context: InsertionContext) {
        val editor = context.editor
        val document = editor.document
        val offset = editor.caretModel.offset
        val textUntilOffset = document.text.run { dropLast(length - offset) }
        val indexOfLastChar = textUntilOffset.indexOfLast { it != ' ' }
        document.deleteString(indexOfLastChar + 1, offset)
    }
}