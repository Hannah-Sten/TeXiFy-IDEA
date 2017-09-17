package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateSettings
import nl.rubensten.texifyidea.lang.BibtexEntryType

/**
 * @author Ruben Schellekens
 */
object TokenTypeInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext?, item: LookupElement?) {
        if (context == null) {
            return
        }

        val inserted = item?.`object` as? BibtexEntryType ?: return
        insertType(inserted, context)
    }

    private fun insertString(inserted: BibtexEntryType, context: InsertionContext) {
        val editor = context.editor
        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset

        document.insertString(offset, "{  }")
        caret.moveToOffset(offset + 2)
    }

    private fun insertPreamble(inserted: BibtexEntryType, context: InsertionContext) {
        val editor = context.editor
        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset

        document.insertString(offset, "{\n    \"\"\n}")
        caret.moveToOffset(offset + 7)
    }

    private fun insertType(inserted: BibtexEntryType, context: InsertionContext) {
        val templateSettings = TemplateSettings.getInstance()
        val template = templateSettings.getTemplateById("BIBTEX.type.${inserted.token}")

        val editor = context.editor
        val templateManager = TemplateManager.getInstance(context.project)
        templateManager.startTemplate(editor, template)
    }
}