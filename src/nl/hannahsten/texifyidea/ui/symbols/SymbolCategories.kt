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

        createCategory("Testcategory") {
            add(LatexMathCommand.DELTA)
            add(LatexMathCommand.HAT, latex = "\\hat{<cursor>}", image = "\\[\n\\hat{a}\n\\]")
            add(LatexRegularCommand.NEWLINE, latex = "\\newline % No you don't want this\n", description = "Bad.")
        }

        createCategory("Another test category") {
            add(LatexRegularCommand.SAGITTARIUS)
        }
    }

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