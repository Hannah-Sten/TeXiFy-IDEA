package nl.hannahsten.texifyidea.run.latex.logtab

class LatexLogMessageExtractor(val text: String, val newText: String, val currentFile: String?) {
    /**
     * Look for a warning or error message in [text].
     * Return null if [text] does not contain such an error or warning.
     */
    fun findMessage(): LatexLogMessage? {
        // Check if we have found an error
        FILE_LINE_ERROR_REGEX.find(text)?.apply {
            val line = groups["line"]?.value?.toInt()?.minus(1)
            val fileName = groups["file"]?.value?.trim()
            val message = groups["message"]?.value?.removeSuffix(newText) ?: ""
            return LatexLogMessage(message, fileName, line, LatexLogMessageType.ERROR)
        }

        // Check if we have found a warning
        if (TEX_WARNINGS.any { text.startsWith(it) }) {
            return LatexLogMessage(text.removeSuffix(newText), fileName = currentFile, type = LatexLogMessageType.WARNING)
        }

        return null
    }

    companion object {
        private val FILE_LINE_ERROR_REGEX = """^(?<file>.+)?:(?<line>\d+): (?<message>.+)$""".toRegex()
        private val TEX_WARNINGS = listOf(
                "LaTeX Warning: ",
                "LaTeX Font Warning: ",
                "AVAIL list clobbered at",
                "Double-AVAIL list clobbered at",
                "Doubly free location at",
                "Bad flag at",
                "Runaway definition",
                "Runaway argument",
                "Runaway text",
                "Missing character: There is no",
                "No pages of output.",
                "Underfull \\hbox",
                "Overfull \\hbox",
                "Loose \\hbox",
                "Tight \\hbox",
                "Underfull \\vbox",
                "Overfull \\vbox",
                "Loose \\vbox",
                "Tight \\vbox"
        )
    }
}