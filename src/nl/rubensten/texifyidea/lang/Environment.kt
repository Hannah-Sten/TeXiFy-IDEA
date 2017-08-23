package nl.rubensten.texifyidea.lang

import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.util.name
import java.util.*

/**
 * @author Ruben Schellekens, Sten Wessel
 */
enum class Environment(
        vararg val arguments: Argument = emptyArray(),
        val envName: String,
        val initialContents: String = "",
        val context: Context = Context.NORMAL,
        val `package`: Package = Package.DEFAULT
) {

    // Vanilla LaTeX
    ABSTRACT(envName = "abstract"),
    ALLTT(envName = "alltt"),
    CENTER(envName = "center"),
    DESCRIPTION(envName = "description", initialContents = "\\item"),
    DISPLAYMATH(envName = "displaymath"),
    DOCUMENT(envName = "document"),
    ENUMERATE(envName = "enumerate", initialContents = "\\item "),
    EQUATION(envName = "equation", context = Context.MATH),
    EQUATION_STAR(envName = "equation*", context = Context.MATH),
    EQNARRAY(envName = "eqnarray"),
    FIGURE(envName = "figure", arguments = OptionalArgument("placement")),
    FIGURE_STAR(envName = "figure*", arguments = OptionalArgument("placement")),
    FILECONTENTS(envName = "filecontents"),
    FILECONTENTS_STAR(envName = "filecontents*"),
    FLUSHLEFT(envName = "flushleft"),
    FLUSHRIGHT(envName = "flushright"),
    FOOTNOTESIZE(envName = "footnotesize"),
    HUGE(envName = "huge"),
    CAPITAL_HUGE(envName = "Huge"),
    ITEMIZE(envName = "itemize", initialContents = "\\item "),
    LARGE(envName = "large"),
    CAPITAL_LARGE(envName = "Large"),
    SCREAMING_LARGE(envName = "LARGE"),
    LIST(RequiredArgument("label"), RequiredArgument("spacing"), envName = "list"),
    LRBOX(envName = "lrbox"),
    MATH(envName = "math"),
    MINIPAGE(OptionalArgument("position"), RequiredArgument("width"), envName = "minipage"),
    NORMALSIZE(envName = "normalsize"),
    QUOTATION(envName = "quotation"),
    QUOTE(envName = "quote"),
    SCRIPTSIZE(envName = "scriptsize"),
    SMALL(envName = "small"),
    TABBING(envName = "tabbing"),
    TABLE(envName = "table", arguments = OptionalArgument("placement")),
    TABLE_STAR(envName = "table*", arguments = OptionalArgument("placement")),
    TABULAR(OptionalArgument("pos"), RequiredArgument("cols"), envName = "tabular"),
    TABULAR_STAR(RequiredArgument("width"), OptionalArgument("pos"), RequiredArgument("cols"), envName = "tabular*"),
    THEBIBLIOGRAPHY(envName = "thebibliography", arguments = RequiredArgument("widestlabel")),
    THEINDEX(envName = "theindex"),
    THEOREM(envName = "theorem", arguments = OptionalArgument("optional")),
    TINY(envName = "tiny"),
    TITLEPAGE(envName = "titlepage"),
    TRIVLIST(envName = "trivlist"),
    VERBATIM(envName = "verbatim"),
    VERBATIM_STAR(envName = "verbatim*"),
    VERSE(envName = "verse"),

    // amsmath
    ALIGN(envName = "align", context = Context.MATH, `package` = Package.AMSMATH),
    ALIGN_STAR(envName = "align*", context = Context.MATH, `package` = Package.AMSMATH),
    ALIGNAT(envName = "alignat", context = Context.MATH, `package` = Package.AMSMATH),
    ALIGNAT_STAR(envName = "alignat*", context = Context.MATH, `package` = Package.AMSMATH),
    FLALIGN(envName = "flalign", context = Context.MATH, `package` = Package.AMSMATH),
    FLALIGN_STAR(envName = "flalign*", context = Context.MATH, `package` = Package.AMSMATH),
    GATHER(envName = "gather", context = Context.MATH, `package` = Package.AMSMATH),
    GATHER_STAR(envName = "gather*", context = Context.MATH, `package` = Package.AMSMATH),
    MULTLINE(envName = "multline", context = Context.MATH, `package` = Package.AMSMATH),
    MULTLINE_STAR(envName = "multline*", context = Context.MATH, `package` = Package.AMSMATH),
    SPLIT(envName = "split", context = Context.MATH, `package` = Package.AMSMATH),
    SPLIT_STAR(envName = "split*", context = Context.MATH, `package` = Package.AMSMATH),
    CASES(envName = "cases", context = Context.MATH, `package` = Package.AMSMATH);

    companion object {

        /**
         * Caches all the enum instances to increase performance.
         */
        private val lookup = HashMap<String, Environment>()

        init {
            for (environment in Environment.values()) {
                lookup.put(environment.envName, environment)
            }
        }

        /**
         * Gets the [Environment] object that corresponds to the given Psi environment.
         */
        fun fromPsi(latexEnvironment: LatexEnvironment): Environment? {
            val text: String = latexEnvironment.name()?.text ?: return null
            return get(text)
        }

        /**
         * Finds the [Environment] object with the given name.
         */
        operator fun get(name: String): Environment? = lookup[name]
    }

    /**
     * @author Ruben Schellekens
     */
    enum class Context {
        NORMAL, MATH
    }
}
