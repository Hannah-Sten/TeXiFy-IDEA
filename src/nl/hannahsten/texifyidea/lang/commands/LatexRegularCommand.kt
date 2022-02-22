package nl.hannahsten.texifyidea.lang.commands

/**
 * @author Hannah Schellekens, Sten Wessel
 */
object LatexRegularCommand {

    val GENERIC: Set<LatexCommand> = LatexGenericRegularCommand.values().toSet()
    val TEXTCOMP: Set<LatexCommand> = LatexTextcompCommand.values().toSet()
    val EURO: Set<LatexCommand> = LatexEuroCommand.values().toSet()
    val TEXT_SYMBOLS: Set<LatexCommand> = LatexTextSymbolCommand.values().toSet()
    val NEW_DEFINITIONS: Set<LatexCommand> = LatexNewDefinitionCommand.values().toSet()
    val MATHTOOLS: Set<LatexCommand> = LatexMathtoolsRegularCommand.values().toSet()
    val XCOLOR: Set<LatexCommand> = LatexColorDefinitionCommand.values().toSet()
    val XPARSE: Set<LatexCommand> = LatexXparseCommand.values().toSet()
    val NATBIB: Set<LatexCommand> = LatexNatbibCommand.values().toSet()
    val BIBLATEX: Set<LatexCommand> = LatexBiblatexCommand.values().toSet()
    val SIUNITX: Set<LatexCommand> = LatexSiunitxCommand.values().toSet()
    val ALGORITHMICX: Set<LatexCommand> = LatexAlgorithmicxCommand.values().toSet()
    val IFS: Set<LatexCommand> = LatexIfCommand.values().toSet()
    val LISTINGS: Set<LatexCommand> = LatexListingCommand.values().toSet()
    val LOREM_IPSUM: Set<LatexCommand> = LatexLoremIpsumCommand.values().toSet()

    val ALL: Set<LatexCommand> = GENERIC + TEXTCOMP + EURO + TEXT_SYMBOLS + NEW_DEFINITIONS + MATHTOOLS +
            XCOLOR + XPARSE + NATBIB + BIBLATEX + SIUNITX + ALGORITHMICX + IFS + LISTINGS + LOREM_IPSUM

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
