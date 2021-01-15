package nl.hannahsten.texifyidea.ui.symbols

import nl.hannahsten.texifyidea.lang.LatexCommand

/**
 * @author Hannah Schellekens
 */
open class DryUiEntry(
        override val description: String,
        override val generatedLatex: String,
        override val fileName: String,
        override val imageLatex: String,
        override val isMathSymbol: Boolean
) : SymbolUiEntry {

    override val command: LatexCommand? = null

    override val imagePath = "/nl/hannahsten/texifyidea/symbols/$fileName"
}