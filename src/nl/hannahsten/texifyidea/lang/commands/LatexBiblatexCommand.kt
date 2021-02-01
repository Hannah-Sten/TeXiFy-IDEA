package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.BIBLATEX

/**
 * @author Hannah Schellekens
 */
enum class LatexBiblatexCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = false,
        val collapse: Boolean = false
) : LatexCommand {

    CITE_CAPITALIZED("Cite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    PARENCITE("parencite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    PARENCITE_CAPITALIZED("Parencite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    FOOTCITE("footcite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    FOOTCITETEXT("footcitetext", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    TEXTCITE("textcite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    TEXTCITE_CAPITALIZED("Textcite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    SMARTCITE("smartcite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    SMARTCITE_CAPITALIZED("Smartcite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    CITE_STAR("cite*", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    PARENCITE_STAR("parencite*", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    SUPERCITE("supercite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    AUTOCITE("autocite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    AUTOCITE_CAPITALIZED("Autocite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    AUTOCITE_STAR("autocite*", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    AUTOCITE_STAR_CAPITALIZED("Autocite*", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    BIBLATEX_CITEAUTHOR("citeauthor", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    BIBLATEX_CITEAUTHOR_STAR("citeauthor*", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    BIBLATEX_CITEAUTHOR_CAPITALIZED("Citeauthor", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    BIBLATEX_CITEAUTHOR_STAR_CAPITALIZED("Citeauthor*", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    BIBLATEX_CITETITLE("citetitle", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    BIBLATEX_CITETITLE_STAR("citetitle*", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    BIBLATEX_CITEYEAR("citeyear", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    BIBLATEX_CITEYEAR_STAR("citeyear*", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    CITEDATE("citedate", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    CITEDATE_STAR("citedate*", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    CITEURL("citeurl", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    VOLCITE("volcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    VOLCITE_CAPITALIZED("Volcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    PVOLCITE("pvolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    PVOLCITE_CAPITALIZED("Pvolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    FVOLCITE("fvolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    FVOLCITE_CAPITALIZED("Fvolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    FTVOLCITE("ftvolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    SVOLCITE("svolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    SVOLCITE_CAPITALIZED("Svolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    TVOLCITE("tvolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    TVOLCITE_CAPITALIZED("Tvolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    AVOLCITE("avolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    AVOLCITE_CAPITALIZED("Avolcite", "prenote".asOptional(), "volume".asRequired(), "page".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    FULLCITE("fullcite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    FOOTFULLCITE("footcullcite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    BIBLATEX_NOCITE("nocite", "key".asRequired(), dependency = BIBLATEX),
    NOTECITE("notecite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    NOTECITE_CAPITALIZED("Notecite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    PNOTECITE("pnotecite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    PNOTECITE_CAPITALIZED("Pnotecite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    FNOTECITE("fnotecite", "prenote".asOptional(), "postnote".asOptional(), "key".asRequired(), dependency = BIBLATEX),
    PARENTTEXT("parenttext", "text".asRequired(Argument.Type.TEXT), dependency = BIBLATEX),
    BRACKETTEXT("brackettext", "text".asRequired(Argument.Type.TEXT), dependency = BIBLATEX),
    ;

    override val identifyer: String
        get() = name
}