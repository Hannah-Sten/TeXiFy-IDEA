package nl.hannahsten.texifyidea.run.latex.logtab

import com.intellij.openapi.diagnostic.Logger
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.LINE_WIDTH
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.lineNumber
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.firstIndexOfAny
import nl.hannahsten.texifyidea.util.remove
import java.util.*

class LatexFileStack(
    vararg val file: String,
    /** Number of open parentheses that do not represent file openings and have not yet been closed. */
    var notClosedNonFileOpenParentheses: Int = 0
) : ArrayDeque<String>() {

    private var shouldSkipNextLine = false

    // Usage: set to true to output all file openings/closings to idea.log
    // Then open the LaTeX log and the relevant part of idea.log in IntelliJ, and start from the idea.log parenthesis which
    // was closed incorrectly, use the LaTeX log and brace matching to find which file should have been closed, then check where it was actually closed, etc.
    private val debug = false
    private val logger = Logger.getInstance("LatexFileStack")

    init {
        addAll(file)
    }

    /** Collect file here if it spans multiple lines. */
    private var currentCollectingFile = ""

    override fun peek(): String? = if (isEmpty()) null else super.peek()

    private fun pushFile(file: String, line: String) {
        push(file)
        if (debug) logger.info("$line ---- Opening $file")
        if (file.containsAny(setOf("(", ")"))) throw TeXception("$file is probably not a valid file")
    }

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
            // Files may end halfway the line, or right at the end of a line
            // Assume there are no spaces in the path (how can we know where the file ends?)
            val endIndex = if (setOf(" ", ")", "(").any { it in line }) line.firstIndexOfAny(' ', ')', '(') else line.length
            currentCollectingFile += line.substring(0, endIndex).remove("\n")
            // Check if this was the last part of the file
            if (line.substring(0, endIndex).length < LINE_WIDTH) {
                // Assume that paths can be quoted, but there are no " in folder/file names
                pushFile(currentCollectingFile.trim('"'), line)
                currentCollectingFile = ""
            }
        }

        // Matches an open par with a filename, or a closing par
        // If the filepath probably continues on the next line, don't try to match the extension
        // (could be improved by actually checking the length of the 'file' group instead of line length)
        // Otherwise, match the file extension to avoid matching just text in parentheses
        val fileRegex = if (line.length >= LINE_WIDTH - 1) {
            Regex("""\((?<file>"?\.*(([/\\])*[\w-. :])+"?)|\)""")
        }
        else {
            Regex("""\((?<file>"?\.*(([/\\])*[\w-. :])+\.(\w{2,10})"?)|\)""")
        }

        var result = fileRegex.find(line)
        var linePart = line

        while (result != null) {
            // If the regex matches an open par (with filename), register file
            if (linePart[result.range.first] == '(') {
                // Count all open pars that are before the found opening par.
                if (linePart.indexOfFirst { it == '(' } in 0..result.range.first) {
                    notClosedNonFileOpenParentheses += linePart.substring(0, result.range.first).count { it == '(' }
                }
                val file = result.groups["file"]?.value?.trim() ?: break

                // Check if file spans multiple lines
                // +1 because the starting ( is not in the group
                // -1 because the newline is not here
                // Files could span exactly the full line width, but not continue on the next line
                if (!file.endsWith(".tex") && (file.length + 1 >= LINE_WIDTH - 1 || (line.length >= LINE_WIDTH && line.trim().endsWith(file)))) {
                    currentCollectingFile += file
                }
                else {
                    pushFile(file, line)
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
                    if (debug) logger.info("$line ---- Closing ${peek()}")
                    if (!isEmpty()) {
                        pop()
                    }
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