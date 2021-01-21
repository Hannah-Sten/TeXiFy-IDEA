package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.NATBIB

/**
 * @author Hannah Schellekens
 */
enum class LatexNatbibCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = false,
        val collapse: Boolean = false
) : LatexCommand {

    CITEP("citep", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEP_STAR("citep*", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITET("citet", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITET_STAR("citet*", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEP_CAPITALIZED("Citep", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEP_STAR_CAPITALIZED("Citep*", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITET_CAPITALIZED("Citet", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITET_STAR_CAPITALIZED("Citet*", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEALP("citealp", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEALP_STAR("citealp*", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEALT("citealt", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEALT_STAR("citealt*", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEALP_CAPITALIZED("Citealp", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEALP_STAR_CAPITALIZED("Citealp*", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEALT_CAPITALIZED("Citealt", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEALT_STAR_CAPITALIZED("Citealt*", "before".asOptional(), "after".asOptional(), "keys".asRequired(), dependency = NATBIB),
    CITEAUTHOR("citeauthor", "keys".asRequired(), dependency = NATBIB),
    CITEAUTHOR_STAR("citeauthor*", "keys".asRequired(), dependency = NATBIB),
    CITEAUTHOR_CAPITALIZED("Citeauthor", "keys".asRequired(), dependency = NATBIB),
    CITEAUTHOR_STAR_CAPITALIZED("Citeauthor*", "keys".asRequired(), dependency = NATBIB),
    CITEYEAR("citeyear", "keys".asRequired(), dependency = NATBIB),
    CITEYEAR_STAR("citeyear*", "keys".asRequired(), dependency = NATBIB),
    CITEYEARPAR("citeyearpar", "keys".asRequired(), dependency = NATBIB),
    CITETITLE("citetitle", "keys".asRequired(), dependency = NATBIB),
    CITETITLE_STAR("citetitle*", "keys".asRequired(), dependency = NATBIB),
    CITENUM("citenum", "key".asRequired(), dependency = NATBIB),
    CITETEXT("citetext", "text".asRequired(), dependency = NATBIB),
    ;

    override val identifyer: String
        get() = name
}