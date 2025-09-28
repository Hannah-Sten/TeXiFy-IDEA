package nl.hannahsten.texifyidea.lang.commands

import arrow.core.NonEmptySet
import arrow.core.getOrNone
import arrow.core.nonEmptySetOf

/**
 * @author Hannah Schellekens, Sten Wessel
 */
object LatexRegularCommand {

    private val GENERIC: Set<LatexCommand> = LatexGenericRegularCommand.entries.toSet()
    private val TEXTCOMP: Set<LatexCommand> = LatexTextcompCommand.entries.toSet()
    private val EURO: Set<LatexCommand> = LatexEuroCommand.entries.toSet()
    private val TEXT_SYMBOLS: Set<LatexCommand> = LatexTextSymbolCommand.entries.toSet()
    private val NEW_DEFINITIONS: Set<LatexCommand> = LatexNewDefinitionCommand.entries.toSet()
    private val MATHTOOLS: Set<LatexCommand> = LatexMathtoolsRegularCommand.entries.toSet()
    private val XCOLOR: Set<LatexCommand> = LatexColorDefinitionCommand.entries.toSet()
    private val XPARSE: Set<LatexCommand> = LatexXparseCommand.entries.toSet()
    private val NATBIB: Set<LatexCommand> = LatexNatbibCommand.entries.toSet()
    private val BIBLATEX: Set<LatexCommand> = LatexBiblatexCommand.entries.toSet()
    private val SIUNITX: Set<LatexCommand> = LatexSiunitxCommand.entries.toSet()
    private val ALGORITHMICX: Set<LatexCommand> = LatexAlgorithmicxCommand.entries.toSet()
    private val IFS: Set<LatexCommand> = LatexIfCommand.entries.toSet()
    private val LISTINGS: Set<LatexCommand> = LatexListingCommand.entries.toSet()
    private val LOREM_IPSUM: Set<LatexCommand> = LatexLoremIpsumCommand.entries.toSet()
    private val GLOSSARY: Set<LatexCommand> = LatexGlossariesCommand.entries.toSet()
    private val TODO: Set<LatexCommand> = LatexTodoCommand.entries.toSet()

    val ALL: Set<LatexCommand> = GENERIC + TEXTCOMP + EURO + TEXT_SYMBOLS + NEW_DEFINITIONS + MATHTOOLS +
        XCOLOR + XPARSE + NATBIB + BIBLATEX + SIUNITX + ALGORITHMICX + IFS + LISTINGS + LOREM_IPSUM + GLOSSARY + TODO

    private val lookup = HashMap<String, NonEmptySet<LatexCommand>>()
    private val lookupDisplay = HashMap<String, NonEmptySet<LatexCommand>>()

    init {
        ALL.forEach { cmd ->
            lookup[cmd.command] = lookup.getOrNone(cmd.command).fold({ nonEmptySetOf(cmd) }, { it + nonEmptySetOf(cmd) })
            if (cmd.display != null) {
                lookupDisplay[cmd.display!!] = lookupDisplay.getOrNone(cmd.display!!).fold({ nonEmptySetOf(cmd) }, { it + nonEmptySetOf(cmd) })
            }
        }
    }

    @JvmStatic
    fun findByDisplay(display: String) = lookupDisplay[display]
}
