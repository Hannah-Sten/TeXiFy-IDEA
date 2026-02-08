package nl.hannahsten.texifyidea.ui.symbols

import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LatexLib

/**
 * @author Hannah Schellekens
 */
class DryUiEntry(
    override val description: String,
    override val generatedLatex: String,
    override val fileName: String,
    override val imageLatex: String,
    override val isMathSymbol: Boolean,
    override val dependency: LatexLib = LatexLib.BASE
) : SymbolUiEntry {

    override val command: LSemanticCommand? = null

    override val imagePath = "/nl/hannahsten/texifyidea/symbols/$fileName"
}