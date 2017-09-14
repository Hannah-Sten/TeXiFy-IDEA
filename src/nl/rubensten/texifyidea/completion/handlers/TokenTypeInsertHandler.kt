package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author Ruben Schellekens
 */
object TokenTypeInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext?, item: LookupElement?) {
        if (context == null) {
            return
        }

        val inserted = item?.`object` as? String ?: return
        when (inserted) {
            "string" -> insertString(inserted, context)
            "preamble" -> insertPreamble(inserted, context)
            else -> insertType(inserted, context)
        }
    }

    private fun insertType(inserted: String, context: InsertionContext) {
        val editor = context.editor
        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset

        document.insertString(offset, "{\n}")
        caret.moveToOffset(offset + 1)
    }

    private fun insertString(inserted: String, context: InsertionContext) {
        val editor = context.editor
        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset

        document.insertString(offset, "{  }")
        caret.moveToOffset(offset + 2)
    }

    private fun insertPreamble(inserted: String, context: InsertionContext) {
        val editor = context.editor
        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset

        document.insertString(offset, "{\n    \"\"\n}")
        caret.moveToOffset(offset + 7)
    }
}