package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.endOffset
import nl.rubensten.texifyidea.util.parentOfType

/**
 * Makes the caret skip to the end of the parent command.
 *
 * @author Ruben Schellekens
 */
object MoveToEndOfCommandHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext?, item: LookupElement?) {
        val editor = context?.editor ?: return
        val caret = editor.caretModel
        val file = context.file
        val element = file.findElementAt(caret.offset) ?: return
        val command = element.parentOfType(LatexCommands::class) ?: return
        caret.moveToOffset(command.endOffset())
    }
}