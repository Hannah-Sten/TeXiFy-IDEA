package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.highlighting.BraceMatchingUtil
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiDocumentManager
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdPairedDelimiters

/**
 * Inserts the right part of left-right command pairs, like `\left( \right)`.
 *
 * @author Hannah Schellekens
 */
object RightInsertHandler : InsertHandler<LookupElement> {

    fun handleInsert(context: InsertionContext, element: LookupElement, semantic: LSemanticCommand) {
        val editor = context.editor

        val delimiter = PredefinedCmdPairedDelimiters.delimiterLeftMap[semantic.name]
        if(delimiter != null) {
            val hasMatchingBrace = BraceMatchingUtil.matchBrace(context.editor.document.text, LatexFileType, editor.highlighter.createIterator(context.editor.caretModel.offset - 1), true)
            PsiDocumentManager.getInstance(editor.project ?: return).doPostponedOperationsAndUnblockDocument(editor.document)
            if (hasMatchingBrace) {
                editor.document.insertString(editor.caretModel.offset, " ")
            } else {
                editor.document.insertString(editor.caretModel.offset, "  \\" + delimiter.right)
            }
            editor.caretModel.moveToOffset(editor.caretModel.offset + 1)
        }
    }

    override fun handleInsert(context: InsertionContext, element: LookupElement) {
        val command = element.`object` as? LSemanticCommand ?: return
        handleInsert(context, element, command)
    }
}