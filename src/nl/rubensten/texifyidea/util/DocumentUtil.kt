package nl.rubensten.texifyidea.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange

/**
 * Gets all the indentation characters of the given lineNumber.
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