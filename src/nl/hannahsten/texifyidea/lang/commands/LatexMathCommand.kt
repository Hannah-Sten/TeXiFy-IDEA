package nl.hannahsten.texifyidea.lang.commands

/**
 * @author Hannah Schellekens, Sten Wessel
 */
object LatexMathCommand {

    val GREEK_ALPHABET: Set<LatexCommand> = LatexGreekCommand.values().toSet()
    val OPERATORS: Set<LatexCommand> = LatexOperatorCommand.values().toSet()
    val MATHTOOLS_COLONEQ: Set<LatexCommand> = LatexColoneqCommand.values().toSet()
    val DELIMITERS: Set<LatexCommand> = LatexDelimiterCommand.values().toSet()
    val ARROWS: Set<LatexCommand> = LatexArrowCommand.values().toSet()
    val GENERIC_COMMANDS: Set<LatexCommand> = LatexGenericMathCommand.values().toSet()
    val UNCATEGORIZED_STMARYRD_SYMBOLS: Set<LatexCommand> = LatexUncategorizedStmaryrdSymbols.values().toSet()

    val ALL: Set<LatexCommand> = GREEK_ALPHABET + OPERATORS + MATHTOOLS_COLONEQ + DELIMITERS + ARROWS +
        GENERIC_COMMANDS + UNCATEGORIZED_STMARYRD_SYMBOLS

    private val lookup = HashMap<String, MutableSet<LatexCommand>>()
    private val lookupDisplay = HashMap<String, MutableSet<LatexCommand>>()

    init {
        ALL.forEach {
            lookup.getOrPut(it.command) { mutableSetOf() }.add(it)
            if (it.display != null) {
                lookupDisplay.putIfAbsent(it.display!!, mutableSetOf(it))?.add(it)
            }
        }
    }

    @JvmStatic
    fun values() = ALL

    @JvmStatic
    operator fun get(command: String) = lookup[command]?.toSet()

    @JvmStatic
    fun findByDisplay(display: String) = lookupDisplay[display]?.toSet()
}