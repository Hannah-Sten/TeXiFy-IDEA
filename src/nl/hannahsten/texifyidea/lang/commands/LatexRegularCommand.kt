package nl.hannahsten.texifyidea.lang.commands

/**
 * @author Hannah Schellekens, Sten Wessel
 */
object LatexRegularCommand {

    private val GENERIC: Set<LatexCommand> = LatexGenericRegularCommand.values().toSet()
    private val TEXTCOMP: Set<LatexCommand> = LatexTextcompCommand.values().toSet()
    private val EURO: Set<LatexCommand> = LatexEuroCommand.values().toSet()
    private val TEXT_SYMBOLS: Set<LatexCommand> = LatexTextSymbolCommand.values().toSet()
    private val NEW_DEFINITIONS: Set<LatexCommand> = LatexNewDefinitionCommand.values().toSet()
    private val MATHTOOLS: Set<LatexCommand> = LatexMathtoolsRegularCommand.values().toSet()
    private val XCOLOR: Set<LatexCommand> = LatexColorDefinitionCommand.values().toSet()
    private val XPARSE: Set<LatexCommand> = LatexXparseCommand.values().toSet()
    private val NATBIB: Set<LatexCommand> = LatexNatbibCommand.values().toSet()
    private val BIBLATEX: Set<LatexCommand> = LatexBiblatexCommand.values().toSet()
    private val SIUNITX: Set<LatexCommand> = LatexSiunitxCommand.values().toSet()
    private val ALGORITHMICX: Set<LatexCommand> = LatexAlgorithmicxCommand.values().toSet()
    private val IFS: Set<LatexCommand> = LatexIfCommand.values().toSet()
    private val LISTINGS: Set<LatexCommand> = LatexListingCommand.values().toSet()
    private val LOREM_IPSUM: Set<LatexCommand> = LatexLoremIpsumCommand.values().toSet()
    private val GLOSSARY: Set<LatexCommand> = LatexGlossariesCommand.values().toSet()

    val ALL: Set<LatexCommand> = GENERIC + TEXTCOMP + EURO + TEXT_SYMBOLS + NEW_DEFINITIONS + MATHTOOLS +
        XCOLOR + XPARSE + NATBIB + BIBLATEX + SIUNITX + ALGORITHMICX + IFS + LISTINGS + LOREM_IPSUM + GLOSSARY

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
