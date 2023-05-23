package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.psi.endOffset
import nl.hannahsten.texifyidea.util.psi.parentOfType

/**
 * Makes the caret skip to the end of the parent command.
 *
 * @author Hannah Schellekens
 */
object MoveToEndOfCommandHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val editor = context.editor
        val caret = editor.caretModel
        val file = context.file
        val element = file.findElementAt(caret.offset) ?: return
        val command = element.parentOfType(LatexCommands::class) ?: return
        caret.moveToOffset(command.endOffset())
    }
}