package nl.hannahsten.texifyidea.run.latex.logtab

/**
 * Regex (text) every error handler and message processor can use.
 */
object LatexLogMagicRegex {

    // Line length (including newline at the end)
    const val LINE_WIDTH = 80

    // Match filename:linenumber: as this probably denotes an error, but not if it appears in a stacktrace
    // and starts with ... and also not if this starts with a ( as then we assume the ( is not part of the file
    const val FILE_LINE_REGEX =
        """(?!\s*\.\.\.)(?<file>[^()]+\.\w+):(?<line>\d+):""" // error
    val REPORTED_ON_LINE_REGEX =
        """( Reported| Found)? on input line (?<line>\d+).""".toRegex()
    const val LINE_REGEX: String =
        """on input line (?<line>\d+).""" // meestal warning
    const val LATEX_ERROR_REGEX: String = "!" // error
    const val PDFTEX_ERROR_REGEX: String = "!pdfTeX error:"
    const val LATEX_WARNING_REGEX: String = "LaTeX( Font)? Warning:" // warning
    const val PACKAGE_REGEX: String =
        """(?<package>[\d\w-.]+)""" // package error/warning?
    const val REFERENCE_REGEX: String =
        """(?<label>([`']).+')""" // reference warning
    const val PACKAGE_WARNING_CONTINUATION = "\\(\\w+\\) {${"Package warning:".length}}"
    const val DUPLICATE_WHITESPACE =
        """\s{2,}"""

    val lineNumber =
        """^l.\d+ """.toRegex()

    /*
     * Errors
     */

    /** A variation on [FILE_LINE_REGEX] by lualatex (?) */
    val directLuaError =
        """^\((?!\s*\.\.\.)(.+\.\w+)\)(\[.+])?:(?<line>\d+): (?<message>.*)""".toRegex()
    val fixMeError =
        """FiXme (Fatal )?Error: '(?<message>.+)' on input line (?<line>\d+).""".toRegex()

    /*
     * Warnings
     */
    val fixMeWarning =
        """FiXme Warning: '(?<message>.+)' on input line (?<line>\d+).""".toRegex()
    val pdfTeXWarning =
        """pdfTeX warning(.+)?: (?<message>.+)""".toRegex()

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
        "No auxiliary output files.",
        "No file ",
        "No pages of output.",
        "Underfull \\hbox",
        "Overfull \\hbox",
        "Loose \\hbox",
        "Tight \\hbox",
        "Underfull \\vbox",
        "Overfull \\vbox",
        "Loose \\vbox",
        "Tight \\vbox",
        "(\\end occurred",
    )
}