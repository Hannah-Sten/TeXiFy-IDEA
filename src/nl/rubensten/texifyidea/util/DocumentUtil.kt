package nl.rubensten.texifyidea.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange

/**
 * Gets all the indentation characters of the line of the given lineNumber.
 *
 * @param lineNumber
 *              The line number of the line to get the indentation of.
 * @return A string containing all the indentation characters. `empty string` when problems arise.
 */
fun Document.lineIndentation(lineNumber: Int): String {
    val result = StringBuilder()

    val lineStart = this.getLineStartOffset(lineNumber)
    val lineEnd = this.getLineEndOffset(lineNumber)
    val line = this.getText(TextRange(lineStart, lineEnd))

    for (i in 0 until line.length) {
        if (line[i] == ' ' || line[i] == '\t') {
            result.append(line[i])
        }
        else {
            break
        }
    }

    return result.toString()
}

/**
 * Gets all the indentation characters of the line at the given offset.
 *
 * @param lineNumber
 *              The offset of the line to get the indentation of.
 * @return A string containing all the indentation characters. `empty string` when problems arise.
 */
fun Document.lineIndentationByOffset(offset: Int) = lineIndentation(getLineNumber(offset))

/**
 * Places the given string into the document over the given range.
 */
fun Document.replaceString(range: TextRange, string: String) = replaceString(range.startOffset, range.endOffset, string)

/**
 * Get the text in the document at range `offset..1`.
 */
operator fun Document.get(offset: Int) = getText(TextRange.from(offset, 1))

/**
 * Get the text in the given range.
 */
operator fun Document.get(range: IntRange) = getText(range.toTextRange())

/**
 * Replaces a single character in the document.
 *
 * @see [Document.replaceString]
 */
operator fun Document.set(offset: Int, value: CharSequence) = replaceString(offset, offset + 1, value)

/**
 * @see [Document.replaceString]
 */
operator fun Document.set(range: IntRange, value: CharSequence) = replaceString(range.start, range.endInclusive, value)

/**
 * Inserts a string into the document and moves the caret to the end of the inserted string.
 *
 * @param offset
 *              Where to insert the string.
 * @param string
 *              The string to insert.
 */
fun Editor.insertAndMove(offset: Int, string: String) {
    val document = this.document
    runWriteAction { document.insertString(offset, string) }
    caretModel.moveToOffset(caretModel.offset + string.length)
}

/**
 * @see [CaretModel.offset]
 */
fun Editor.caretOffset() = caretModel.offset