package nl.hannahsten.texifyidea.lang.graphic

import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.lang.LatexLib

/**
 * @author Hannah Schellekens
 */
enum class FigureLocation(
    val symbol: String,
    override val description: String,
    val requiredPackage: LatexLib? = null
) : Described {

    TOP("t", "top"),
    BOTTOM("b", "bottom"),
    PAGE("p", "page"),
    HERE("h", "here"),
    STRICT_HERE("H", "strict here", requiredPackage = LatexLib.FLOAT),
    OVERRIDE_INTERNAL_PARAMETERS("!", "override");

    override fun toString() = description

    companion object {

        val ALL_SYMBOLS = entries.mapNotNull { it.symbol.firstOrNull() }

        fun bySymbol(symbol: String) = entries.firstOrNull { it.symbol == symbol }
    }
}