package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.highlighting.BraceMatchingUtil
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiDocumentManager
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexDelimiterCommand
import nl.hannahsten.texifyidea.lang.predefined.PredefinedPairedDelimiters

/**
 * Inserts the right part of left-right command pairs, like `\left( \right)`.
 *
 * @author Hannah Schellekens
 */
open class RightInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, element: LookupElement) {
        val editor = context.editor
        val command = element.`object` as? LatexCommand ?: return

        if (command is LatexDelimiterCommand && command.isLeft) {
            val hasMatchingBrace = BraceMatchingUtil.matchBrace(context.editor.document.text, LatexFileType, editor.highlighter.createIterator(context.editor.caretModel.offset - 1), true)
            PsiDocumentManager.getInstance(editor.project ?: return).doPostponedOperationsAndUnblockDocument(editor.document)
            if (hasMatchingBrace) {
                editor.document.insertString(editor.caretModel.offset, " ")
            } else {
                editor.document.insertString(editor.caretModel.offset, "  \\" + command.matchingName)
            }
            editor.caretModel.moveToOffset(editor.caretModel.offset + 1)
        }
    }
}



/**
 * Inserts the right part of left-right command pairs, like `\left( \right)`.
 *
 * @author Hannah Schellekens
 */
object SemanticRightInsertHandler : InsertHandler<LookupElement> {

    fun handleInsert(context: InsertionContext, element: LookupElement, semantic: LSemanticCommand) {
        val editor = context.editor

        val delimiter = PredefinedPairedDelimiters.delimiterLeftMap[semantic.name]
        if( delimiter != null) {
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
        handleInsert(context, element,command)

    }
}