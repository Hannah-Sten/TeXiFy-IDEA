package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.util.parser.parentOfType

/**
 * @author Hannah Schellekens
 */
class LatexCommandArgumentInsertHandler(val arguments: List<Argument>? = null) : InsertHandler<LookupElement> {

    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        insert(insertionContext, lookupElement)
    }

    private fun insert(context: InsertionContext, lookupElement: LookupElement) {
        val editor = context.editor
        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset
        // When not followed by { or [ (whichever the first parameter starts with) insert the parameters.
        if (arguments != null && (
                offset >= document.textLength - 1 || document.getText(TextRange.from(offset, 1)) !in setOf("{", "[")
                )
        ) {
            insertParametersLiveTemplate(editor)
        }
        else {
            skipParameters(editor, lookupElement)
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

    private fun skipParameters(editor: Editor, lookupElement: LookupElement) {
        val document = editor.document
        val file = document.psiFile(editor.project ?: return)
        val caret = editor.caretModel
        val extraSpaces = lookupElement.lookupString.takeLastWhile { it == ' ' }.length
        val psiElement = file?.findElementAt(caret.offset + extraSpaces) ?: return
        if (psiElement.text in setOf("{", "[")) {
            caret.moveToOffset((psiElement.parentOfType(LatexCommands::class)?.endOffset() ?: return) - extraSpaces)
        }
    }
}