package nl.hannahsten.texifyidea.index.file

/**
 * Do some basic formatting on documentation strings found in dtx files.
 * Only done when indexing, but it should still be fast because it can be done up to 28714 times for full TeX Live.
 * Should format to valid HTML as used in the docs popup.
 *
 * @author Thomas
 */
object LatexDocsFormattingRegexes {

    private val replacers = listOf(
        // marg and oarg from ltxdoc. Slight assumption: commands are only preceded by a space
        Pair("""[^|]\\(marg)\{(?<command>.+?)}""".toRegex(), { result -> " {<" + result.groups["command"]?.value + ">}" }),
        Pair("""[^|]\\(oarg)\{(?<command>.+?)}""".toRegex(), { result -> " [<" + result.groups["command"]?.value + ">]" }),
        // \cs command from the doctools package
        Pair("""[^|]\\(cs|cn)\{(?<command>.+?)}""".toRegex(), { result -> " \\" + result.groups["command"]?.value }),
        // Any other commands, hopefully like \textbf, \emph etc
        Pair<Regex, (MatchResult) -> String>("""[^|]\\[a-zA-Z_:]+?\{(?<argument>.+?)}""".toRegex(), { result -> " " + result.groups["argument"]?.value ?: "" }),
        // Short verbatim, provided by ltxdoc
        Pair("""\|""".toRegex(), { "" }),
    )

    fun format(docs: String): String {
        var formatted = docs
        replacers.forEach { formatted = it.first.replace(formatted, it.second) }
        return formatted
    }
}