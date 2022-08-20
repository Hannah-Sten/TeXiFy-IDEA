package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.inspections.latex.codestyle.LatexLineBreakInspection
import java.util.regex.Pattern

typealias RegexPattern = Pattern

/**
 * @author Hannah Schellekens
 */
object PatternMagic {

    val ellipsis = Regex("""(?<!\.)(\.\.\.)(?!\.)""")

    /**
     * This is the only correct way of using en dashes.
     */
    val correctEnDash = RegexPattern.compile("[0-9]+--[0-9]+")!!

    /**
     * Matches the prefix of a label. Amazing comment this is right?
     */
    val labelPrefix = RegexPattern.compile(".*:")!!

    /**
     * Matches the end of a sentence.
     *
     * Includes `[^.][^.]` because of abbreviations (at least in Dutch) like `s.v.p.`
     */
    const val sentenceEndPrefix = "[^.A-Z][^.A-Z]"
    val sentenceEnd = RegexPattern.compile("($sentenceEndPrefix[.?!;;] +[^%\\s])|(^\\. )")!!

    /**
     * Matches all interpunction that marks the end of a sentence.
     */
    val sentenceSeparator = RegexPattern.compile("[.?!;;]")!!

    /**
     * Matches all sentenceSeparators at the end of a line (with or without space).
     */
    val sentenceSeparatorAtLineEnd = RegexPattern.compile("$sentenceSeparator\\s*$")!!

    /**
     * Matches when a string ends with a non breaking space.
     */
    val endsWithNonBreakingSpace = RegexPattern.compile("~$")!!

    /**
     * Finds all abbreviations that have at least two letters separated by comma's.
     *
     * It might be more parts, like `b.v.b.d. ` is a valid abbreviation. Likewise are `sajdflkj.asdkfj.asdf ` and
     * `i.e. `. Single period abbreviations are not being detected as they can easily be confused with two letter words
     * at the end of the sentence (also localisation...) For this there is a quickfix in [LatexLineBreakInspection].
     */
    val abbreviation = RegexPattern.compile("[0-9A-Za-z.]+\\.[A-Za-z](\\.[\\s~])")!!

    /**
     * Abbreviations not detected by [PatternMagic.abbreviation].
     */
    val unRegexableAbbreviations = listOf(
        "et al."
    )

    /** [abbreviation]s that are missing a normal space (or a non-breaking space) */
    val abbreviationWithoutNormalSpace = RegexPattern.compile("[0-9A-Za-z.]+\\.[A-Za-z](\\.[\\s])")!!

    /**
     * Matches all comments, starting with % and ending with a newline.
     */
    val comments: RegexPattern = RegexPattern.compile("%(.*)\\n")

    /**
     * Matches leading and trailing whitespace on a string.
     */
    val excessWhitespace = RegexPattern.compile("(^(\\s+).*(\\s*)\$)|(^(\\s*).*(\\s+)\$)")!!

    /**
     * Matches a non-ASCII character.
     */
    val nonAscii = RegexPattern.compile("\\P{ASCII}")!!

    /**
     * Separator for multiple parameter values in one parameter.
     *
     * E.g. when you have \cite{citation1,citation2,citation3}, this pattern will match the separating
     * comma.
     */
    val parameterSplit = RegexPattern.compile("\\s*,\\s*")!!

    /**
     * Matches the extension of a file name.
     */
    val fileExtension = RegexPattern.compile("\\.[a-zA-Z0-9]+$")!!

    /**
     * Matches all leading whitespace.
     */
    val leadingWhitespace = RegexPattern.compile("^\\s*")!!

    /**
     * Matches newlines.
     */
    val newline = RegexPattern.compile("\\n")!!

    /**
     * Checks if the string is `text`, two newlines, `text`.
     */
    val containsMultipleNewlines = RegexPattern.compile("[^\\n]*\\n\\n+[^\\n]*")!!

    /**
     * Matches HTML tags of the form `<tag>`, `<tag/>` and `</tag>`.
     */
    val htmlTag = RegexPattern.compile("""<.*?/?>""")!!

    /**
     * Matches a LaTeX command that is the start of an \if-\fi structure.
     */
    val ifCommand = RegexPattern.compile("\\\\if[a-zA-Z@]*")!!

    /**
     * Matches the begin and end commands of the cases and split environments.
     */
    val casesOrSplitCommands = Regex(
        "((?=\\\\begin\\{cases})|(?<=\\\\begin\\{cases}))" +
            "|((?=\\\\end\\{cases})|(?<=\\\\end\\{cases}))" +
            "|((?=\\\\begin\\{split})|(?<=\\\\begin\\{split}))" +
            "|((?=\\\\end\\{split})|(?<=\\\\end\\{split}))"
    )

    /**
     * Matches any consecutive sequence of LaTeX quote characters
     */
    val quotePattern = """["'`]+""".toRegex()
}
