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

    if (quoteSetting == TexifySettings.QuoteReplacement.LIGATURES && char == '"') {
        openingQuotes = "``"
        closingQuotes = "''"
    }
    else if (quoteSetting == TexifySettings.QuoteReplacement.COMMANDS && char == '"') {
        openingQuotes = "\\lq\\lq{}"
        closingQuotes = "\\rq\\rq{}"
    }
    else if (quoteSetting == TexifySettings.QuoteReplacement.CSQUOTES && char == '"') {
        openingQuotes = "\\enquote{"
        closingQuotes = "}"
    }
    else if (quoteSetting == TexifySettings.QuoteReplacement.LIGATURES && char == '\'') {
        openingQuotes = "`"
        closingQuotes = "'"
    }
    else if (quoteSetting == TexifySettings.QuoteReplacement.COMMANDS && char == '\'') {
        openingQuotes = "\\lq{}"
        closingQuotes = "\\rq{}"
    }
    else if (quoteSetting == TexifySettings.QuoteReplacement.CSQUOTES && char == '\'') {
        openingQuotes = "\\enquote*{"
        closingQuotes = "}"
    }

    return Pair(openingQuotes, closingQuotes)
}