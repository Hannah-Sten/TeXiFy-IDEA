package nl.rubensten.texifyidea.intentions.latexmathtoggle

class OneLiner(private val equation: String) {
    fun getOneLiner(): String = reduce()

    private fun isMultiLine(): Boolean = equation.contains("\n")

    /**
     * Reduce the content of a multi line environment to 'one' line.
     *
     * Removes all line breaks and alignment characters, except those inside a cases or split
     * environment. Changes all occurrences of '\intertext{}' to '\text{}'.
     */
    private fun reduce(): String {

        // Split the string at every begin/end command for the cases/split environments.
        val splitByBeginEnd: MutableList<String> = equation.split(
                Regex("((?=\\\\begin\\{cases})|(?<=\\\\begin\\{cases}))" +
                        "|((?=\\\\end\\{cases})|(?<=\\\\end\\{cases}))" +
                        "|((?=\\\\begin\\{split})|(?<=\\\\begin\\{split}))" +
                        "|((?=\\\\end\\{split})|(?<=\\\\end\\{split}))")).toMutableList()

        fun isBeginCommand(text: String): Boolean = text.contains("\\begin")
        fun isEndCommand(text: String): Boolean = text.contains("\\end")

        fun parse(text: String) {
            val index = splitByBeginEnd.indexOf(text)
            splitByBeginEnd[index] = text
                    .replace("\n", "")    // Remove new lines.
                    .replace("\\\\", "")  // Remove end-of-line alignment characters.
                    .replace("&", "")     // Remove alignment characters.
                    .replace("\\intertext", "\\text")
        }

        // Only parse text outside of cases and split environments.
        var depth = 0
        splitByBeginEnd.forEach {
            if (!isBeginCommand(it) && !isEndCommand(it) && depth == 0) parse(it)
            else if (isBeginCommand(it)) depth++
            else if (isEndCommand(it)) depth--
        }
        return splitByBeginEnd.joinToString(separator = "")
    }
}