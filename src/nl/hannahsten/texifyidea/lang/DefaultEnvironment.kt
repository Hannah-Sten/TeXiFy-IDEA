package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.lang.Environment.Context
import nl.hannahsten.texifyidea.lang.Package.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.Package.Companion.GAUSS
import nl.hannahsten.texifyidea.lang.Package.Companion.MATHTOOLS
import nl.hannahsten.texifyidea.lang.Package.Companion.XCOLOR
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.name
import java.util.*

/**
 * @author Hannah Schellekens, Sten Wessel
 */
enum class DefaultEnvironment(
        override vararg val arguments: Argument,
        override val environmentName: String,
        override val initialContents: String = "",
        override val context: Context = Context.NORMAL,
        override val dependency: Package = Package.DEFAULT
) : Environment {

    // Vanilla LaTeX
    ABSTRACT(environmentName = "abstract"),
    ALLTT(environmentName = "alltt"),
    CENTER(environmentName = "center"),
    DESCRIPTION(environmentName = "description", initialContents = "\\item "),
    DISPLAYMATH(environmentName = "displaymath", context = Context.MATH),
    DOCUMENT(environmentName = "document"),
    ENUMERATE(environmentName = "enumerate", initialContents = "\\item "),
    EQUATION(environmentName = "equation", context = Context.MATH),
    EQNARRAY(environmentName = "eqnarray"),
    FIGURE(environmentName = "figure", arguments = *arrayOf(OptionalArgument("placement"))),
    FIGURE_STAR(environmentName = "figure*", arguments = *arrayOf(OptionalArgument("placement"))),
    FILECONTENTS(environmentName = "filecontents"),
    FILECONTENTS_STAR(environmentName = "filecontents*"),
    FLUSHLEFT(environmentName = "flushleft"),
    FLUSHRIGHT(environmentName = "flushright"),
    FOOTNOTESIZE(environmentName = "footnotesize"),
    HUGE(environmentName = "huge"),
    CAPITAL_HUGE(environmentName = "Huge"),
    ITEMIZE(environmentName = "itemize", initialContents = "\\item "),
    LARGE(environmentName = "large"),
    CAPITAL_LARGE(environmentName = "Large"),
    SCREAMING_LARGE(environmentName = "LARGE"),
    LIST(RequiredArgument("label"), RequiredArgument("spacing"), environmentName = "list"),
    LRBOX(environmentName = "lrbox"),
    MATH(environmentName = "math"),
    MINIPAGE(OptionalArgument("position"), RequiredArgument("width"), environmentName = "minipage"),
    NORMALSIZE(environmentName = "normalsize"),
    QUOTATION(environmentName = "quotation"),
    QUOTE(environmentName = "quote"),
    SCRIPTSIZE(environmentName = "scriptsize"),
    SMALL(environmentName = "small"),
    TABBING(environmentName = "tabbing"),
    TABLE(environmentName = "table", arguments = *arrayOf(OptionalArgument("placement"))),
    TABLE_STAR(environmentName = "table*", arguments = *arrayOf(OptionalArgument("placement"))),
    TABULAR(OptionalArgument("pos"), RequiredArgument("cols"), environmentName = "tabular"),
    TABULAR_STAR(RequiredArgument("width"), OptionalArgument("pos"), RequiredArgument("cols"), environmentName = "tabular*"),
    THEBIBLIOGRAPHY(environmentName = "thebibliography", arguments = *arrayOf(RequiredArgument("widestlabel"))),
    THEINDEX(environmentName = "theindex"),
    THEOREM(environmentName = "theorem", arguments = *arrayOf(OptionalArgument("optional"))),
    TINY(environmentName = "tiny"),
    TITLEPAGE(environmentName = "titlepage"),
    TRIVLIST(environmentName = "trivlist"),
    VERBATIM(environmentName = "verbatim"),
    VERBATIM_STAR(environmentName = "verbatim*"),
    VERSE(environmentName = "verse"),

    // amsmath
    ALIGN(environmentName = "align", context = Context.MATH, dependency = AMSMATH),
    ALIGNAT(environmentName = "alignat", context = Context.MATH, dependency = AMSMATH),
    ALIGNAT_STAR(environmentName = "alignat*", context = Context.MATH, dependency = AMSMATH),
    ALIGNED(environmentName = "aligned", context = Context.MATH, dependency = AMSMATH),
    ALIGNEDAT(environmentName = "alignedat", context = Context.MATH, dependency = AMSMATH),
    ALIGN_STAR(environmentName = "align*", context = Context.MATH, dependency = AMSMATH),
    BMATRIX(environmentName = "bmatrix", context = Context.MATH, dependency = AMSMATH),
    BMATRIX_CAPITAL(environmentName = "Bmatrix", context = Context.MATH, dependency = AMSMATH),
    CASES(environmentName = "cases", context = Context.MATH, dependency = AMSMATH),
    EQUATION_STAR(environmentName = "equation*", context = Context.MATH, dependency = AMSMATH),
    FLALIGN(environmentName = "flalign", context = Context.MATH, dependency = AMSMATH),
    FLALIGN_STAR(environmentName = "flalign*", context = Context.MATH, dependency = AMSMATH),
    GATHER(environmentName = "gather", context = Context.MATH, dependency = AMSMATH),
    GATHERED(environmentName = "gathered", context = Context.MATH, dependency = AMSMATH),
    GATHER_STAR(environmentName = "gather*", context = Context.MATH, dependency = AMSMATH),
    MATRIX(environmentName = "matrix", context = Context.MATH, dependency = AMSMATH),
    MULTLINE(environmentName = "multline", context = Context.MATH, dependency = AMSMATH),
    MULTLINE_STAR(environmentName = "multline*", context = Context.MATH, dependency = AMSMATH),
    PMATRIX(environmentName = "pmatrix", context = Context.MATH, dependency = AMSMATH),
    SMALLMATRIX(environmentName = "smallmatrix", context = Context.MATH, dependency = AMSMATH),
    SPLIT(environmentName = "split", context = Context.MATH, dependency = AMSMATH),
    SUBARRAY(environmentName = "subarray", context = Context.MATH, dependency = AMSMATH),
    SUBEQUATIONS(environmentName = "subequations", context = Context.MATH, dependency = AMSMATH),
    VMATRIX(environmentName = "vmatrix", context = Context.MATH, dependency = AMSMATH),
    VMATRIX_CAPITAL(environmentName = "Vmatrix", context = Context.MATH, dependency = AMSMATH),
    XALIGNAT(environmentName = "xalignat", context = Context.MATH, dependency = AMSMATH),
    XALIGNAT_STAR(environmentName = "xalignat*", context = Context.MATH, dependency = AMSMATH),
    XXALIGNAT(environmentName = "xxalignat", context = Context.MATH, dependency = AMSMATH),

    // mathtools
    MATRIX_STAR(environmentName = "matrix*", context = Context.MATH, dependency = MATHTOOLS),
    PMATRIX_STAR(environmentName = "pmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    BMATRIX_STAR(environmentName = "bmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    BMATRIX_CAPITAL_STAR(environmentName = "Bmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    VMATRIX_STAR(environmentName = "vmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    VMATRIX_CAPITAL_STAR(environmentName = "Vmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    SMALLMATRIX_STAR(environmentName = "smallmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    PSMALLMATRIX(environmentName = "psmallmatrix", context = Context.MATH, dependency = MATHTOOLS),
    PSMALLMATRIX_STAR(environmentName = "psmallmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    BSMALLMATRIX(environmentName = "bsmallmatrix", context = Context.MATH, dependency = MATHTOOLS),
    BSMALLMATRIX_STAR(environmentName = "bsmallmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    BSMALLMATRIX_CAPITAL(environmentName = "Bsmallmatrix", context = Context.MATH, dependency = MATHTOOLS),
    BSMALLMATRIX_CAPITAL_STAR(environmentName = "Bsmallmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    VSMALLMATRIX(environmentName = "vsmallmatrix", context = Context.MATH, dependency = MATHTOOLS),
    VSMALLMATRIX_STAR(environmentName = "vsmallmatrix*", context = Context.MATH, dependency = MATHTOOLS),
    VSMALLMATRIX_CAPITAL(environmentName = "Vsmallmatrix", context = Context.MATH, dependency = MATHTOOLS),
    VSMALLMATRIX_CAPITAL_STAR(environmentName = "Vsmallmatrix*", context = Context.MATH, dependency = MATHTOOLS),

    // gauss
    GMATRIX(environmentName = "gmatrix", context = Context.MATH, dependency = GAUSS),

    // comment
    COMMENT(environmentName = "comment", context = Context.COMMENT, dependency = Package.COMMENT),

    // lualatex
    LUACODE(environmentName = "luacode", dependency = Package.LUACODE),

    // listings
    LISTINGS(environmentName = "lstlisting", dependency = Package.LISTINGS),

    // tikz
    TIKZPICTURE(environmentName = "tikzpicture", dependency = Package.TIKZ),

    // xcolor
    TESTCOLORS(environmentName = "testcolors", arguments = *arrayOf(OptionalArgument("num models")), dependency = XCOLOR);


    companion object {

        /**
         * Caches all the enum instances to increase performance.
         */
        private val lookup = HashMap<String, DefaultEnvironment>()

        init {
            for (environment in values()) {
                lookup[environment.environmentName] = environment
            }
        }

        /**
         * Gets the [DefaultEnvironment] object that corresponds to the given Psi environment.
         */
        @JvmStatic
        fun fromPsi(latexEnvironment: LatexEnvironment): DefaultEnvironment? {
            val text: String = latexEnvironment.name()?.text ?: return null
            return get(text.toLowerCase())
        }

        /**
         * Finds the [DefaultEnvironment] object with the given name.
         */
        @JvmStatic
        operator fun get(name: String): DefaultEnvironment? = lookup[name]
    }

}
