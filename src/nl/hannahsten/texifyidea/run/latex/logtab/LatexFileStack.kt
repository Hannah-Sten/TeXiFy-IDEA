package nl.hannahsten.texifyidea.run.latex.logtab

import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.LINE_WIDTH
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.lineNumber
import nl.hannahsten.texifyidea.util.remove
import java.util.ArrayDeque

class LatexFileStack(
    vararg val file: String,
    /** Number of open parentheses that do not represent file openings and have not yet been closed. */
    var notClosedNonFileOpenParentheses: Int = 0
) : ArrayDeque<String>() {

    private var shouldSkipNextLine = false

    init {
        addAll(file)
    }

    /** Collect file here if it spans multiple lines. */
    private var currentCollectingFile = ""

    override fun peek(): String? = if (isEmpty()) null else super.peek()

    /**
     * AFTER all messages have been filtered, look for file openings and closings.
     *
     * Assume that all parenthesis come in pairs. Will probably fail the (few)
     * cases where they don't...
     * (It works for rubber: https://github.com/tsgates/die/blob/master/bin/parse-latex-log.py)
     */
    fun update(line: String): LatexFileStack {

        // Lines starting with a line number seem to contain user content, as well as the next line (which could be empty
        // as well, but in that case it isn't interesting either)
        if (lineNumber.containsMatchIn(line)) {
            shouldSkipNextLine = true
            return this
        }

        if (shouldSkipNextLine) {
            shouldSkipNextLine = false
            return this
        }

        if (currentCollectingFile.isNotEmpty()) {
            // Files may end halfway the line
            val endIndex = if (")" in line) line.indexOf(')') else line.length
            currentCollectingFile += line.substring(0, endIndex).remove("\n")
            // Check if this was the last part of the file
            if (line.substring(0, endIndex).length < LINE_WIDTH) {
                // Assume that paths can be quoted, but there are no " in folder/file names
                push(currentCollectingFile.trim('"'))
                currentCollectingFile = ""
            }
        }

        // Matches an open par with a filename, or a closing par
        // If the filepath probably continues on the next line, don't try to match the extension
        // (could be improved by actually checking the length of the 'file' group instead of line length)
        // Otherwise, match the file extension to avoid matching just text in parentheses
        val fileRegex = if (line.length >= LINE_WIDTH - 1) {
            Regex("""\((?<file>"?\.*(([/\\])*[\w-\d. :])+"?)|\)""")
        }
        else {
            Regex("""\((?<file>"?\.*(([/\\])*[\w-\d. :])+\.(\w{2,10})"?)|\)""")
        }

        var result = fileRegex.find(line)
        var linePart = line

        while (result != null) {
            // If the regex matches an open par (with filename), register file
            if (linePart[result.range.first] == '(') {
                val file = result.groups["file"]?.value?.trim() ?: break

                // Check if file spans multiple lines
                // +1 because the starting ( is not in the group
                // -1 because the newline is not here
                if (file.length + 1 >= LINE_WIDTH - 1 || (line.length >= LINE_WIDTH && line.trim().endsWith(file))) {
                    currentCollectingFile += file
                }
                else {
                    push(file)
                }
            }

            // Regex has matched a closing par
            else {
                // Count all open pars that are before the found closing par.
                if (linePart.indexOfFirst { it == '(' } in 0..result.range.first) {
                    notClosedNonFileOpenParentheses += linePart.substring(0, result.range.first).count { it == '(' }
                }
                if (notClosedNonFileOpenParentheses > 0) notClosedNonFileOpenParentheses--
                else {
                    if (isEmpty()) {
                        throw TeXception("Extra closing parenthesis: could not close a file which was not opened. Please report the log output to the issue tracker.")
                    }
                    pop()
                }
            }
            linePart = linePart.substring(result.range.last + 1)
            result = fileRegex.find(linePart)
        }

        // When we find a closing par or no match, there can still be an open
        // parenthesis somewhere on the current line (before the closing par).
        // We want to detect this par, so we know that the next closing par does
        // not close a file.
        // This has to happen after the above while loop, to still catch leftover open brackets at the end of a line
        if (result == null) {
            notClosedNonFileOpenParentheses += linePart.count { it == '(' }
        }

        return this
    }
}