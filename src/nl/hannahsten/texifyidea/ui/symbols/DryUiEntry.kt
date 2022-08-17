package nl.hannahsten.texifyidea.ui.symbols

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand

/**
 * @author Hannah Schellekens
 */
class DryUiEntry(
        override val description: String,
        override val generatedLatex: String,
        override val fileName: String,
        override val imageLatex: String,
        override val isMathSymbol: Boolean,
        override val dependency: LatexPackage = LatexPackage.DEFAULT
) : SymbolUiEntry {

    override val command: LatexCommand? = null

    override val imagePath = "/nl/hannahsten/texifyidea/symbols/$fileName"
}