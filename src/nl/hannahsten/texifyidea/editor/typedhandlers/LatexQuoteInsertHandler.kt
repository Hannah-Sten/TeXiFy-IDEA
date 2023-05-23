package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.getOpenAndCloseQuotes
import nl.hannahsten.texifyidea.util.psi.inVerbatim
import nl.hannahsten.texifyidea.util.insertUsepackage
import kotlin.math.min

/**
 * This class performs smart quote substitution. When this is enabled, it will replace double quotes " and single quotes ' with the appropriate LaTeX symbols.
 *
 * @author Thomas Schouten
 */
open class LatexQuoteInsertHandler : TypedHandlerDelegate() {

    override fun charTyped(char: Char, project: Project, editor: Editor, file: PsiFile): Result {
        // Only do this for latex files and if the option is enabled
        if (file.fileType != LatexFileType || TexifySettings.getInstance().automaticQuoteReplacement == TexifySettings.QuoteReplacement.NONE) {
            return super.charTyped(char, project, editor, file)
        }

        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset

        // Disable in verbatim context.
        val psi = file.findElementAt(offset)
        if (psi?.inVerbatim() == true) return super.charTyped(char, project, editor, file)

        // Only do smart things with double and single quotes
        if (char != '"' && char != '\'') {
            return super.charTyped(char, project, editor, file)
        }

        // Check if we are not out of the document range
        if (offset - 1 < 0 || offset - 1 >= document.textLength) {
            return super.charTyped(char, project, editor, file)
        }

        insertReplacement(document, file, caret, offset, char)

        return super.charTyped(char, project, editor, file)
    }

    /**
     * Insert either opening or closing quotes.
     *
     * This behaviour is inspired by the smart quotes functionality of TeXworks, source:
     * https://github.com/TeXworks/texworks/blob/2f902e2e429fad3e2bbb56dff07c823d1108adf4/src/CompletingEdit.cpp#L762
     */
    private fun insertReplacement(document: Document, file: PsiFile, caret: CaretModel, offset: Int, char: Char) {
        val replacementPair = getOpenAndCloseQuotes(char)
        val openingQuotes = replacementPair.first
        val closingQuotes = if (TexifySettings.getInstance().automaticQuoteReplacement == TexifySettings.QuoteReplacement.CSQUOTES) "" else replacementPair.second

        // The default replacement of the typed double quotes is a pair of closing quotes
        var isOpeningQuotes = false

        // Always use opening quotes at the beginning of the document
        if (offset == 1) {
            isOpeningQuotes = true
        }
        else {
            // Character before the cursor
            val previousChar = document.getText(TextRange.from(offset - 2, 1))

            if ((previousChar.firstOrNull())?.isLetter() == true && char == '\'') return

            // Don't replace when trying to type an escaped quote \"
            if (previousChar == "\\") return

            // Assume that if the previous char is a space, we are not closing anything
            if (previousChar == " ") {
                isOpeningQuotes = true
            }

            // After opening brackets, also use opening quotes
            if (previousChar == "{" || previousChar == "[" || previousChar == "(") {
                isOpeningQuotes = true
            }

            // If we are not closing the quotes, assume we are opening it (instead of doing nothing)
            if (TexifySettings.getInstance().automaticQuoteReplacement == TexifySettings.QuoteReplacement.CSQUOTES &&
                document.getText(TextRange.from(min(offset, document.textLength - 1), 1)) != "}"
            ) {
                isOpeningQuotes = true
            }
        }

        val replacement = if (isOpeningQuotes) openingQuotes else closingQuotes

        // Insert the replacement of the typed character, recall the offset is now right behind the inserted char
        document.deleteString(offset - 1, offset)
        document.insertString(offset - 1, replacement)

        // Move the cursor behind the replacement which replaced the typed char
        caret.moveToOffset(min(offset + replacement.length - 1, document.textLength))

        handleCsquotesInsertion(document, file, isOpeningQuotes, caret, char)
    }

    /**
     * Special behaviour for \enquote, because it is a command, not just opening and closing quotes.
     */
    private fun handleCsquotesInsertion(document: Document, file: PsiFile, isOpeningQuotes: Boolean, caret: CaretModel, char: Char) {
        if (TexifySettings.getInstance().automaticQuoteReplacement == TexifySettings.QuoteReplacement.CSQUOTES) {
            if (isOpeningQuotes) {
                // Insert } after the cursor to close the command
                document.insertString(caret.offset, "}")
            }
            else {
                // Instead of typing closing quotes, skip over the closing }
                caret.moveToOffset(caret.offset + 1)
            }

            // Package dependencies
            if (char == '"') {
                file.insertUsepackage(LatexGenericRegularCommand.ENQUOTE.dependency)
            }
            else {
                file.insertUsepackage(LatexGenericRegularCommand.ENQUOTE_STAR.dependency)
            }
        }
    }
}