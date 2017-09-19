package nl.rubensten.texifyidea.insight

/**
 * @author Ruben Schellekens
 */
enum class InsightGroup(

        /**
         * The name that gets displayed in the inspection settings.
         */
        val displayName: String,

        /**
         * The prefix of all internal inspection names.
         */
        val prefix: String
) {

    LATEX("LaTeX", "Latex"),
    BIBTEX("BibTeX", "Bibtex")
}