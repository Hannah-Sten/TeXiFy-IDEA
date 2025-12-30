package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.settings.TexifySettings

/**
 * Define what the replacements are for opening and closing quotes, in case that is relevant for the user setting.
 */
fun getOpenAndCloseQuotes(char: Char): Pair<String, String> {
    var openingQuotes = char.toString()
    var closingQuotes = char.toString()

    // Get the saved value to find the correct replacement
    val quoteSetting = TexifySettings.getState().automaticQuoteReplacement

    when (quoteSetting) {
        TexifySettings.QuoteReplacement.LIGATURES if char == '"' -> {
            openingQuotes = "``"
            closingQuotes = "''"
        }
        TexifySettings.QuoteReplacement.COMMANDS if char == '"' -> {
            openingQuotes = "\\lq\\lq{}"
            closingQuotes = "\\rq\\rq{}"
        }
        TexifySettings.QuoteReplacement.CSQUOTES if char == '"' -> {
            openingQuotes = "\\enquote{"
            closingQuotes = "}"
        }
        TexifySettings.QuoteReplacement.LIGATURES if char == '\'' -> {
            openingQuotes = "`"
            closingQuotes = "'"
        }
        TexifySettings.QuoteReplacement.COMMANDS if char == '\'' -> {
            openingQuotes = "\\lq{}"
            closingQuotes = "\\rq{}"
        }
        TexifySettings.QuoteReplacement.CSQUOTES if char == '\'' -> {
            openingQuotes = "\\enquote*{"
            closingQuotes = "}"
        }
        else -> {}
    }

    return Pair(openingQuotes, closingQuotes)
}