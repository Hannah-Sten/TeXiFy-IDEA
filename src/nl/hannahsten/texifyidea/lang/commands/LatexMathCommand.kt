package nl.hannahsten.texifyidea.lang.commands

import arrow.core.NonEmptySet
import arrow.core.getOrNone
import arrow.core.nonEmptySetOf

/**
 * @author Hannah Schellekens, Sten Wessel
 */
object LatexMathCommand {

    private val GREEK_ALPHABET: Set<LatexCommand> = LatexGreekCommand.values().toSet()
    private val OPERATORS: Set<LatexCommand> = LatexOperatorCommand.values().toSet()
    private val MATHTOOLS_COLONEQ: Set<LatexCommand> = LatexColoneqCommand.values().toSet()
    private val DELIMITERS: Set<LatexCommand> = LatexDelimiterCommand.values().toSet()
    private val ARROWS: Set<LatexCommand> = LatexArrowCommand.values().toSet()
    private val GENERIC_COMMANDS: Set<LatexCommand> = LatexGenericMathCommand.values().toSet()
    private val UNCATEGORIZED_STMARYRD_SYMBOLS: Set<LatexCommand> = LatexUncategorizedStmaryrdSymbols.values().toSet()

    private val ALL: Set<LatexCommand> = GREEK_ALPHABET + OPERATORS + MATHTOOLS_COLONEQ + DELIMITERS + ARROWS +
        GENERIC_COMMANDS + UNCATEGORIZED_STMARYRD_SYMBOLS

    private val lookup = HashMap<String, NonEmptySet<LatexCommand>>()
    private val lookupDisplay = HashMap<String, NonEmptySet<LatexCommand>>()

    init {
        ALL.forEach { cmd ->
            lookup[cmd.command] = lookup.getOrNone(cmd.command).fold({ nonEmptySetOf(cmd) }, { it + nonEmptySetOf(cmd) } )
            if (cmd.display != null) {
                lookupDisplay[cmd.display!!] = lookupDisplay.getOrNone(cmd.display!!).fold({ nonEmptySetOf(cmd) }, { it + nonEmptySetOf(cmd) } )
            }
        }
    }

    @JvmStatic
    fun values() = ALL

    @JvmStatic
    operator fun get(command: String) = lookup[command]

    @JvmStatic
    fun findByDisplay(display: String) = lookupDisplay[display]
}