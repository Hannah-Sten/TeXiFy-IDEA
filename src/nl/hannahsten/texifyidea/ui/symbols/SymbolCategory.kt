package nl.hannahsten.texifyidea.ui.symbols

import nl.hannahsten.texifyidea.lang.Described

/**
 * @author Hannah Schellekens
 */
data class SymbolCategory(val name: String, override val description: String) : Described {

    override fun toString() = name

    companion object {

        val ALL = SymbolCategory("All", "Contains all available symbols.")
    }
}