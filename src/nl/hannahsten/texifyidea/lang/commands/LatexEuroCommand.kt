package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.EUROSYM

/**
 * @author Hannah Schellekens
 */
enum class LatexEuroCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = false,
        val collapse: Boolean = false
) : LatexCommand {

    EURO_SYMBOL("euro", dependency = EUROSYM, display = "€"),
    EURO_AMOUNT("EUR", "amount".asRequired(), dependency = EUROSYM, display = "€"),
    EURO("geneuro", dependency = EUROSYM, display = "€"),
    EURO_NARROW("geneuronarrow", dependency = EUROSYM, display = "€"),
    EURO_WIDE("geneurowide", dependency = EUROSYM, display = "€"),
    OFFICIAL_EURO("officialeuro", dependency = EUROSYM, display = "€"),
    ;

    override val identifyer: String
        get() = name
}