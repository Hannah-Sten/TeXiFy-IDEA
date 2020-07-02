package nl.hannahsten.texifyidea.run.latex.logtab

import java.util.ArrayDeque

class LatexFileStack(vararg val file: String, var nonFileParCount: Int = 0) : ArrayDeque<String>() {
    init {
        addAll(file)
    }

    override fun peek(): String? = if (isEmpty()) null else super.peek()

    /**
     * AFTER all messages have been filtered, look for file openings and closings.
     *
     * Assume that all parenthesis come in pairs. Will probably fail the (few)
     * cases where they don't...
     * (It works for rubber: https://github.com/tsgates/die/blob/master/bin/parse-latex-log.py)
     */
    fun update(line: String): LatexFileStack {
        val fileRegex = Regex("""\((?<file>"?\.*(([/\\])*[\w-\d. :])+\.(\w{2,10})"?)|\)""")

        var result = fileRegex.find(line)
        var linePart = line

        while (result != null) {
            if (linePart[result.range.first] == '(') {
                push(result.groups["file"]?.value ?: break)
            }
            else {
                // Count all open pars that are before the found closing par.
                if (linePart.indexOfFirst { it == '(' } in 0..result.range.first) {
                    nonFileParCount += linePart.substring(0, result.range.first).count { it == '(' }
                }
                if (nonFileParCount > 0) nonFileParCount--
                else {
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
            nonFileParCount += line.count { it == '(' }
        }

        return this
    }
}