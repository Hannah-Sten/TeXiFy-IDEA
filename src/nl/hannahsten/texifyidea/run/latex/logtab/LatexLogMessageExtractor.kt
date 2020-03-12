package nl.hannahsten.texifyidea.run.latex.logtab

object LatexLogMessageExtractor {
    /**
     * Pre-processing to check if line is worth looking at.
     *
     * A line is not worth looking at when:
     *   - it starts with "latexmk:" or "Latexmk:"
     */
    fun skip(text: String?): Boolean {
        return text == null || PARENS_MESSAGES_TO_BE_SKIPPED.containsMatchIn(text)
    }

    /**
     * Look for a warning or error message in [text].
     * Return null if [text] does not contain such an error or warning.
     */
    fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        // Check if we have found an error
        FILE_LINE_ERROR_REGEX.find(text)?.apply {
            val line = groups["line"]?.value?.toInt()
            val fileName = groups["file"]?.value?.trim()
            val message = groups["message"]?.value?.removeSuffix(newText) ?: ""
            return LatexLogMessage(message, fileName, line, LatexLogMessageType.ERROR)
        }

        // Check if we have found a warning
        if (TEX_MISC_WARNINGS.any { text.startsWith(it) }) {
            return LatexLogMessage(text.removeSuffix(newText), fileName = currentFile, type = LatexLogMessageType.WARNING)
        }

        return null
    }

    /*
    re_loghead    = re.compile("This is [0-9a-zA-Z-]*(TeX|Omega)")
re_rerun      = re.compile("LaTeX Warning:.*Rerun")
re_file       = re.compile("(\\((?P<file>[^ \n\t(){}]*)|\\))")
re_badbox     = re.compile(r"(Ov|Und)erfull \\[hv]box ")
re_line       = re.compile(r"(l\.(?P<line>[0-9]+)( (?P<code>.*))?$|<\*>)")
re_cseq       = re.compile(r".*(?P<seq>\\[^ ]*) ?$")
re_page       = re.compile("\[(?P<num>[0-9]+)\]")
re_atline     = re.compile("( detected| in paragraph)? at lines? (?P<line>[0-9]*)(--(?P<last>[0-9]*))?")
re_reference  = re.compile("LaTeX Warning: Reference `(?P<ref>.*)' on page (?P<page>[0-9]*) undefined on input line (?P<line>[0-9]*)\\.$")
re_citation   = re.compile("^.*Citation `(?P<cite>.*)' on page (?P<page>[0-9]*) undefined on input line (?P<line>[0-9]*)\\.$")
re_label      = re.compile("LaTeX Warning: (?P<text>Label .*)$")
re_warning    = re.compile("(LaTeX|Package)( (?P<pkg>.*))? Warning: (?P<text>.*)$")
re_online     = re.compile("(; reported)? on input line (?P<line>[0-9]*)")
re_ignored    = re.compile("; all text was ignored after line (?P<line>[0-9]*).$")

     */
    private val FILE_LINE_ERROR_REGEX = """^(?<file>.+)?:(?<line>\d+): (?<message>.+)$""".toRegex()

    private val PARENS_MESSAGES_TO_BE_SKIPPED =
            """(This is [0-9a-zA-Z-]*(TeX|Omega))|(Please \(re\)run)|(\(\w*\))|(Latexmk: Summary of warnings from last run of \(pdf\)latex:)|(Collected error summary \(may duplicate other messages\):)|(Latex failed to resolve \d+ reference\(s\))|(Latex failed to resolve \d+ citation\(s\))|(\(see the transcript file for additional information\))""".toRegex()

    private val TEX_MISC_WARNINGS = listOf(
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