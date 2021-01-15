package nl.hannahsten.texifyidea.ui.symbols

import nl.hannahsten.texifyidea.lang.Described

/**
 * @author Hannah Schellekens
 */
data class SymbolCategory(val name: String, override val description: String) : Described