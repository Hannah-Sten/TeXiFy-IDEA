package nl.hannahsten.texifyidea.ui.symbols

import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdMath
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdMathSymbols
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdPairedDelimiters
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdTextSymbols
import nl.hannahsten.texifyidea.util.formatAsFileName
import nl.hannahsten.texifyidea.util.repeat

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
            addCommands(PredefinedCmdMathSymbols.defaultOperatorSymbols, PredefinedCmdMathSymbols.mathToolsColoneq)
            add(DryUiEntry(description = "plus sign", "+", "misc_plus.png", "+", true))
            add(DryUiEntry(description = "minus sign", "-", "misc_minus.png", "-", true))
            add(DryUiEntry(description = "factorial", "!", "misc_factorial.png", "!", true))
            add(DryUiEntry(description = "equals sign", "=", "misc_equals.png", "=", true))
            add(DryUiEntry(description = "less than", "<", "misc_lesser.png", "<", true))
            add(DryUiEntry(description = "greater than", ">", "misc_greater.png", ">", true))
        }

        createCategory("Math functions") {
            addCommands(PredefinedCmdMathSymbols.mathTextOperators)
        }

        createCategory("Arrows") {
            addCommands(PredefinedCmdMathSymbols.arrows)
        }

        createCategory("Delimiters") {
            // Place the enclosing delimiters at the front:
            // 1. Makes the symbol overview less cluttered.
            // 2. Encourages usage of \left\right over standalone symbols.
            add(DryUiEntry(description = "brackets", "\\left[ <caret> \\right]", "misc_brackets_pair.png", "\\left[...\\right]", true))
            PredefinedCmdPairedDelimiters.delimiters.forEach { delimiter ->
                addLeftRight(
                    PredefinedCmdPairedDelimiters.delimiterCommands.first { it.name == delimiter.left },
                    PredefinedCmdPairedDelimiters.delimiterCommands.first { it.name == delimiter.right },
                    requireLeftRight = delimiter.left !in listOf("llceil", "llfloor", "llparenthesis", "lbag", "Lbag") && "left" !in delimiter.left
                )
            }

            // Single delimiters.
            add(DryUiEntry(description = "left bracket", "[", "misc_left_bracket.png", "[", true))
            add(DryUiEntry(description = "right bracket", "]", "misc_right_bracket.png", "]", true))
            add(DryUiEntry(description = "vertical line", "|", "misc_vertical_line.png", "|", true))
            add(DryUiEntry(description = "left parenthesis", "(", "misc_left_parenthesis.png", "(", true))
            add(DryUiEntry(description = "right parenthesis", ")", "misc_right_parenthesis.png", ")", true))
            addCommands(PredefinedCmdPairedDelimiters.delimiterCommands)
        }

        createCategory("Greek") {
            PredefinedCmdMathSymbols.let { addCommands(it.lowercaseGreek, it.uppercaseGreek, it.variantGreek, it.upgreekCommand) }

            add(DryUiEntry(description = "omicron", "o", "misc_omicron.png", "o", true))
            add(DryUiEntry(description = "capital alpha", "A", "misc_capital_alpha.png", "A", true))
            add(DryUiEntry(description = "capital beta", "B", "misc_capital_beta.png", "B", true))
            add(DryUiEntry(description = "capital epsilon", "E", "misc_capital_epsilon.png", "E", true))
            add(DryUiEntry(description = "capital zeta", "Z", "misc_capital_zeta.png", "Z", true))
            add(DryUiEntry(description = "capital eta", "Z", "misc_capital_eta.png", "H", true))
            add(DryUiEntry(description = "capital iota", "I", "misc_capital_iota.png", "I", true))
            add(DryUiEntry(description = "capital kappa", "K", "misc_capital_kappa.png", "K", true))
            add(DryUiEntry(description = "capital mu", "M", "misc_capital_mu.png", "M", true))
            add(DryUiEntry(description = "capital nu", "M", "misc_capital_nu.png", "N", true))
            add(DryUiEntry(description = "capital omicron", "O", "misc_capital_omicron.png", "O", true))
            add(DryUiEntry(description = "capital rho", "P", "misc_capital_rho.png", "P", true))
            add(DryUiEntry(description = "capital tau", "T", "misc_capital_tau.png", "T", true))
            add(DryUiEntry(description = "capital chi", "X", "misc_capital_chi.png", "X", true))
        }

        createCategory("Misc. math") {
            addCommands(PredefinedCmdMath.defaultMathArgCommands)

            PredefinedCmdMathSymbols.let { addCommands(it.defaultMathSymbols, it.amssymbMathSymbols) }
        }

        createCategory("Text") {
            PredefinedCmdTextSymbols.let { addCommands(it.textSymbols, it.euro, it.textcomp) }

            (0..9).forEach {
                add(
                    DryUiEntry(
                        description = "old style enum $it",
                        generatedLatex = "\\oldstylenums{$it}",
                        fileName = "text_oldstylenums_$it.png",
                        imageLatex = "\\oldstylenums{$it}",
                        isMathSymbol = false,
                        dependency = LatexLib.BASE
                    )
                )
            }

            PredefinedCmdTextSymbols.escapedSymbols.forEach {
                add(it, fileName = "text_escaped_${it.description.formatAsFileName()}.png")
            }
        }

        createCategory("Misc. symbols") {
            // "The Windings of LaTeX" -Sten 2021
            addCommands(PredefinedCmdMathSymbols.uncategorizedStmaryrdSymbols, PredefinedCmdMathSymbols.wasysymMathSymbols, PredefinedCmdTextSymbols.generalSymbols)
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
     * Adds a symbol entry for a Left/Right pair that inserts \leftX ... \rightX.
     *
     * @param requireLeftRight
     *          True if the commands should be preceded with \left and \right. False if the commands alone suffice.
     */
    private fun MutableList<SymbolUiEntry>.addLeftRight(
        left: LSemanticCommand, right: LSemanticCommand, requireLeftRight: Boolean = true
    ) {
        val leftCmd = if (requireLeftRight) "\\left" else ""
        val rightCmd = if (requireLeftRight) "\\right" else ""
        add(
            DryUiEntry(
                description = "$leftCmd...$rightCmd",
                generatedLatex = "$leftCmd${left.commandWithSlash} <caret> $rightCmd${right.commandWithSlash}",
                fileName = "math_${left.name.formatAsFileName()}.png",
                imageLatex = "$leftCmd${left.commandWithSlash}...$rightCmd${right.commandWithSlash}",
                isMathSymbol = true,
                dependency = left.dependency
            )
        )
    }

    private fun MutableList<SymbolUiEntry>.addCommands(vararg commands: List<LSemanticCommand>) {
        commands.forEach { commandsList ->
            commandsList.forEach { command ->
                if (command.arguments.isNotEmpty()) {
                    add(
                        command,
                        latex = command.commandWithSlash + "{<caret>}".repeat(command.arguments.size),
                        image = command.commandWithSlash + listOf("a", "b", "c", "d", "e", "f", "g", "h", "i").take(command.arguments.size).joinToString(separator = "") { "{$it}" })
                }
                else {
                    add(command)
                }
            }
        }
    }

    /**
     * Adds a UI entry for the given command to the entry list.
     * For the parameters see [CommandUiEntry].
     */
    private fun MutableList<SymbolUiEntry>.add(
        command: LSemanticCommand,
        latex: String? = null,
        fileName: String? = null,
        description: String? = null,
        image: String? = null
    ) = add(command.toEntry(latex, fileName, description, image))

    /**
     * Turns the command into a ui entry.
     */
    private fun LSemanticCommand.toEntry(
        latex: String? = null,
        fileName: String? = null,
        description: String? = null,
        image: String? = null
    ) = CommandUiEntry(this, latex, fileName, description, image)

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