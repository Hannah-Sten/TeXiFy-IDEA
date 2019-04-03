package nl.rubensten.texifyidea.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.file.LatexFileType
import nl.rubensten.texifyidea.settings.TexifySettings

/**
 * This class performs smart quote substitution. When this is enabled, it will replace double quotes " and single quotes ' with the appropriate LaTeX symbols.
 *
 * @author Thomas Schouten
 */
open class LatexQuoteInsertHandler : TypedHandlerDelegate() {

    override fun charTyped(char: Char, project: Project, editor: Editor, file: PsiFile): Result {

        // Only do this for latex files and if the option is enabled
        if (file.fileType != LatexFileType || TexifySettings.getInstance().automaticQuoteReplacement == 0) {
            return super.charTyped(char, project, editor, file)
        }

        val document = editor.document
        val caret = editor.caretModel
        val offset = caret.offset

        // Only do smart things with double and single quotes
        if (char != '"' && char != '\'') {
            return super.charTyped(char, project, editor, file)
        }

        // Check if we are not out of the document range
        if (offset - 1 < 0 || offset + 1 >= document.textLength) {
            return super.charTyped(char, project, editor, file)
        }

        // This behaviour is inspired by the smart quotes functionality of TeXworks, source:
        // https://github.com/TeXworks/texworks/blob/2f902e2e429fad3e2bbb56dff07c823d1108adf4/src/CompletingEdit.cpp#L762

        var openingQuotes = ""
        var closingQuotes = ""

        // Use hard-coded order of options: 0 is off, 1 is TeX ligatures, 2 is TeX commands
        val quoteSetting = TexifySettings.getInstance().automaticQuoteReplacement

        if (quoteSetting == 1 && char == '"') {
            openingQuotes = "``"
            closingQuotes = "''"
        }
        else if (quoteSetting == 2 && char == '"') {
            openingQuotes = "\\lq\\lq{}"
            closingQuotes = "\\rq\\rq{}"
        }
        else if (quoteSetting == 1 && char == '\'') {
            openingQuotes = "`"
            closingQuotes = "'"
        }
        else if (quoteSetting == 2 && char == '\'') {
            openingQuotes = "\\lq{}"
            closingQuotes = "\\rq{}"
        }

        // The default replacement of the typed double quotes is a pair of closing quotes
        var replacement = closingQuotes

        // Always use opening quotes at the beginning of the document
        if (offset == 1) {
            replacement = openingQuotes
        }
        else {
            // Character before the cursor
            val previousChar = document.getText(TextRange.from(offset - 2, 1))

            // Assume that if the previous char is a space, we are not closing anything
            if (previousChar == " ") {
                replacement = openingQuotes
            }

            // After opening brackets, also use opening quotes
            if (previousChar == "{" || previousChar == "[" || previousChar == "(") {
                replacement = openingQuotes
            }
        }

        // Insert the replacement of the typed character, recall the offset is now right behind the inserted char
        document.deleteString(offset - 1, offset)
        document.insertString(offset - 1, replacement)

        // Move the cursor behind the replacement which replaced the typed char
        caret.moveToOffset(offset + replacement.length - 1)

        return super.charTyped(char, project, editor, file)
    }
}