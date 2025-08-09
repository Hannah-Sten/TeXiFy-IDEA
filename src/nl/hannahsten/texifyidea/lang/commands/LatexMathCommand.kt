package nl.hannahsten.texifyidea.lang.commands

import arrow.core.NonEmptySet
import arrow.core.nonEmptySetOf
import nl.hannahsten.texifyidea.lang.LatexPackage

/**
 * @author Hannah Schellekens, Sten Wessel, Florian Kraft
 */
object LatexMathCommand {

    private val GREEK_ALPHABET: Set<LatexCommand> = LatexGreekCommand.entries.toSet()
    private val MATHTOOLS_COLONEQ: Set<LatexCommand> = LatexColoneqCommand.entries.toSet()
    private val DELIMITERS: Set<LatexCommand> = LatexDelimiterCommand.entries.toSet()
    private val ARROWS: Set<LatexCommand> = LatexArrowCommand.entries.toSet()
    private val GENERIC_COMMANDS: Set<LatexCommand> = LatexGenericMathCommand.entries.toSet()
    private val UNCATEGORIZED_STMARYRD_SYMBOLS: Set<LatexCommand> = LatexUncategorizedStmaryrdSymbols.entries.toSet()
    private val DIFFCOEFF: Set<LatexCommand> = LatexDiffcoeffCommand.entries.toSet()
    private val UPGREEK: Set<LatexCommand> = LatexUpgreekCommand.entries.toSet()
    private val REGULAR_ALSO_IN_MATH =
        setOf(LatexGenericRegularCommand.BEGIN, LatexGenericRegularCommand.END, LatexGenericRegularCommand.LABEL)

    private val ALL: Set<LatexCommand> = emptySet()
//        GREEK_ALPHABET + OPERATORS + MATHTOOLS_COLONEQ + DELIMITERS + ARROWS +
//        GENERIC_COMMANDS + UNCATEGORIZED_STMARYRD_SYMBOLS + DIFFCOEFF + UPGREEK + REGULAR_ALSO_IN_MATH

    val defaultCommands: Set<LatexCommand> = ALL.filter { it.dependency == LatexPackage.DEFAULT }.toSet()

    private val lookup: Map<String, NonEmptySet<LatexCommand>> = buildMap {
        ALL.forEach { cmd ->
            merge(cmd.command, nonEmptySetOf(cmd), NonEmptySet<LatexCommand>::plus)
        }
    }
    private val lookupDisplay: Map<String, NonEmptySet<LatexCommand>> = buildMap {
        ALL.forEach { cmd ->
            cmd.display?.let { display ->
                merge(display, nonEmptySetOf(cmd), NonEmptySet<LatexCommand>::plus)
            }
        }
    }

    private val lookupWithSlash: Map<String, NonEmptySet<LatexCommand>> = lookup.mapKeys { "\\${it.key}" }

    val lookupFromPackage: Map<String, Set<LatexCommand>> = ALL
        .groupBy { it.dependency.name }
        .mapValues { it.value.toSet() }

    @JvmStatic
    fun values() = ALL

    @JvmStatic
    operator fun get(command: String) = lookup[command]

    @JvmStatic
    fun getWithSlash(command: String) = lookupWithSlash[command]

    @JvmStatic
    fun findByDisplay(display: String) = lookupDisplay[display]
}