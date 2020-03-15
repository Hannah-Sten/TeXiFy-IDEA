package nl.hannahsten.texifyidea.run.latex.logtab

abstract class LatexMessageHandler(val messageType: LatexLogMessageType, vararg val regex: Regex) {
    abstract fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage?

    /**
     * Regex (text) every handler can use.
     */
    companion object {
        val FILE_LINE_REGEX: String = """(?<file>.+)?:(?<line>\d+):"""
        val LINE_REGEX: String = """on input line (?<line>\d+)."""
        val LATEX_ERROR_REGEX: String = "! LaTeX Error:"
        val LATEX_WARNING_REGEX: String = "LaTeX Warning:"
        val PACKAGE_REGEX: String = """(?<package>[\d\w]+)"""
        val REFERENCE_REGEX: String = """(?<label>(`|').+')"""

        val TEX_MISC_WARNINGS = listOf(
                "LaTeX Warning: ",
                "LaTeX Font Warning: ",
                "AVAIL list clobbered at",
                "Citation",
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