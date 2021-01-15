package nl.hannahsten.texifyidea.ui.symbols

import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.LatexMathCommand
import nl.hannahsten.texifyidea.lang.LatexRegularCommand

/**
 * @author Hannah Schellekens
 */
object SymbolCategories {

    /**
     * Maps each category to the symbols that are in the category.
     *
     * The categories are ordered, as are the symbols per category.
     */
    val categories: Map<SymbolCategory, List<SymbolUiEntry>> = LinkedHashMap<SymbolCategory, List<SymbolUiEntry>>().apply {

        createCategory("Operators") {
            add(LatexMathCommand.DELTA)
            add(LatexMathCommand.HAT, latex = "\\hat{<caret>}", image = "\\hat{a}")
            add(LatexRegularCommand.NEWLINE, latex = "\\newline % No you don't want this\n", description = "Bad.")
            add(LatexMathCommand.DELTA)
            add(LatexMathCommand.HAT, latex = "\\hat{<caret>}", image = "\\hat{a}")
            add(LatexRegularCommand.NEWLINE, latex = "\\newline % No you don't want this\n", description = "Bad.")
            add(LatexMathCommand.DELTA)
            add(LatexMathCommand.HAT, latex = "\\hat{<caret>}", image = "\\hat{a}")
            add(LatexRegularCommand.NEWLINE, latex = "\\newline % No you don't want this\n", description = "Bad.")
            add(LatexMathCommand.DELTA)
            add(LatexMathCommand.HAT, latex = "\\hat{<caret>}", image = "\\hat{a}")
            add(LatexRegularCommand.NEWLINE, latex = "\\newline % No you don't want this\n", description = "Bad.")
            add(LatexMathCommand.DELTA)
            add(LatexMathCommand.HAT, latex = "\\hat{<caret>}", image = "\\hat{a}")
            add(LatexRegularCommand.NEWLINE, latex = "\\newline % No you don't want this\n", description = "Bad.")
            add(LatexMathCommand.DELTA)
            add(LatexMathCommand.HAT, latex = "\\hat{<caret>}", image = "\\hat{a}")
            add(LatexRegularCommand.NEWLINE, latex = "\\newline % No you don't want this\n", description = "Bad.")
        }

        createCategory("Misc. Symbols") {
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
            add(LatexRegularCommand.SAGITTARIUS)
        }
    }

    /**
     * The list of all registered categories.
     * Also contains the ALL category.
     */
    val categoryList: List<SymbolCategory> = listOf(SymbolCategory.ALL) + categories.map { (category, _) -> category }

    /**
     * Flat map of all registered symbols, in order.
     */
    val symbolList: List<SymbolUiEntry> = categories.flatMap { it.value }

    /**
     * Get the operators that are in the given category.
     * The category [SymbolCategory.ALL] returns all available symbols.
     */
    operator fun get(category: SymbolCategory): List<SymbolUiEntry> = if (category == SymbolCategory.ALL) {
        symbolList
    }
    else categories[category] ?: emptyList()

    /**
     * Adds a UI entry for the given command to the entry list.
     * For the parameters see [CommandUiEntry].
     */
    private fun MutableList<SymbolUiEntry>.add(
            command: LatexCommand,
            latex: String? = null,
            fileName: String? = null,
            description: String? = null,
            image: String? = null
    ) = add(CommandUiEntry(command, latex, fileName, description, image))

    /**
     * Adds a new category to the map and initializes the symbols.
     */
    private fun MutableMap<SymbolCategory, List<SymbolUiEntry>>.createCategory(
            name: String,
            description: String = name,
            symbolInitializer: MutableList<SymbolUiEntry>.() -> Unit
    ) {
        val category = SymbolCategory(name, description)
        this[category] = ArrayList<SymbolUiEntry>().apply {
            symbolInitializer()
        }
    }
}