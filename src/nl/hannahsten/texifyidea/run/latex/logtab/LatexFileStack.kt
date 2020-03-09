package nl.hannahsten.texifyidea.run.latex.logtab

import java.util.*

class LatexFileStack(vararg val file: String) : ArrayDeque<String>() {
    init {
        addAll(file)
    }

    override fun peek(): String? = if (isEmpty()) null else super.peek()

    /**
     * AFTER all messages have been filtered, look for file openings and closings.
     *
     * For closing, accept ALL ')' as file closers. Let's see how well this works...
     * (It works for rubber: https://github.com/tsgates/die/blob/master/bin/parse-latex-log.py)
     */
    fun update(line: String): LatexFileStack {
        val fileRegex = Regex("""(\((?<file>\.*(/[\w-\d]+)+\.(\w{3}))|\))""")

        var result = fileRegex.find(line)
        var linePart = line
        while(result != null) {
            if (linePart[result.range.first] == '(') {
                push(result.groups["file"]?.value ?: break)
            }
            else {
                pop()
            }
            linePart = linePart.substring(result.range.last + 1)
            result = fileRegex.find(linePart)
        }
        return this
    }
}