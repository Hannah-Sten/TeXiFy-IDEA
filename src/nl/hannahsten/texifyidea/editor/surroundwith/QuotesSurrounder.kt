package nl.hannahsten.texifyidea.editor.surroundwith

import nl.hannahsten.texifyidea.util.getOpenAndCloseQuotes

/**
 * Surrounder to surround a selection with double opening and closing quotes, dependent
 * on the user setting.
 *
 * @author Abby Berkers
 */
open class QuotesSurrounder(val char: Char) : LatexPairSurrounder(getOpenAndCloseQuotes(char))

class DoubleQuotesSurrounder : QuotesSurrounder('"')

class SingleQuotesSurrounder : QuotesSurrounder('\'')