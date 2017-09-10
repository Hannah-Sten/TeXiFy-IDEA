package nl.rubensten.texifyidea.lang

import nl.rubensten.texifyidea.lang.Environment.Context
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.util.name
import java.util.*

/**
 * @author Ruben Schellekens, Sten Wessel
 */
enum class DefaultEnvironment(
        private vararg val myArguments: Argument = emptyArray(),
        private val myEnvName: String,
        private val myInitialContents: String = "",
        private val myContext: Context = Context.NORMAL,
        private val myPackage: Package = Package.DEFAULT
) : Environment {

    // Vanilla LaTeX
    ABSTRACT(myEnvName = "abstract"),
    ALLTT(myEnvName = "alltt"),
    CENTER(myEnvName = "center"),
    DESCRIPTION(myEnvName = "description", myInitialContents = "\\item"),
    DISPLAYMATH(myEnvName = "displaymath"),
    DOCUMENT(myEnvName = "document"),
    ENUMERATE(myEnvName = "enumerate", myInitialContents = "\\item "),
    EQUATION(myEnvName = "equation", myContext = Context.MATH),
    EQUATION_STAR(myEnvName = "equation*", myContext = Context.MATH),
    EQNARRAY(myEnvName = "eqnarray"),
    FIGURE(myEnvName = "figure", myArguments = OptionalArgument("placement")),
    FIGURE_STAR(myEnvName = "figure*", myArguments = OptionalArgument("placement")),
    FILECONTENTS(myEnvName = "filecontents"),
    FILECONTENTS_STAR(myEnvName = "filecontents*"),
    FLUSHLEFT(myEnvName = "flushleft"),
    FLUSHRIGHT(myEnvName = "flushright"),
    FOOTNOTESIZE(myEnvName = "footnotesize"),
    HUGE(myEnvName = "huge"),
    CAPITAL_HUGE(myEnvName = "Huge"),
    ITEMIZE(myEnvName = "itemize", myInitialContents = "\\item "),
    LARGE(myEnvName = "large"),
    CAPITAL_LARGE(myEnvName = "Large"),
    SCREAMING_LARGE(myEnvName = "LARGE"),
    LIST(RequiredArgument("label"), RequiredArgument("spacing"), myEnvName = "list"),
    LRBOX(myEnvName = "lrbox"),
    MATH(myEnvName = "math"),
    MINIPAGE(OptionalArgument("position"), RequiredArgument("width"), myEnvName = "minipage"),
    NORMALSIZE(myEnvName = "normalsize"),
    QUOTATION(myEnvName = "quotation"),
    QUOTE(myEnvName = "quote"),
    SCRIPTSIZE(myEnvName = "scriptsize"),
    SMALL(myEnvName = "small"),
    TABBING(myEnvName = "tabbing"),
    TABLE(myEnvName = "table", myArguments = OptionalArgument("placement")),
    TABLE_STAR(myEnvName = "table*", myArguments = OptionalArgument("placement")),
    TABULAR(OptionalArgument("pos"), RequiredArgument("cols"), myEnvName = "tabular"),
    TABULAR_STAR(RequiredArgument("width"), OptionalArgument("pos"), RequiredArgument("cols"), myEnvName = "tabular*"),
    THEBIBLIOGRAPHY(myEnvName = "thebibliography", myArguments = RequiredArgument("widestlabel")),
    THEINDEX(myEnvName = "theindex"),
    THEOREM(myEnvName = "theorem", myArguments = OptionalArgument("optional")),
    TINY(myEnvName = "tiny"),
    TITLEPAGE(myEnvName = "titlepage"),
    TRIVLIST(myEnvName = "trivlist"),
    VERBATIM(myEnvName = "verbatim"),
    VERBATIM_STAR(myEnvName = "verbatim*"),
    VERSE(myEnvName = "verse"),

    // amsmath
    ALIGN(myEnvName = "align", myContext = Context.MATH, myPackage = Package.AMSMATH),
    ALIGN_STAR(myEnvName = "align*", myContext = Context.MATH, myPackage = Package.AMSMATH),
    ALIGNAT(myEnvName = "alignat", myContext = Context.MATH, myPackage = Package.AMSMATH),
    ALIGNAT_STAR(myEnvName = "alignat*", myContext = Context.MATH, myPackage = Package.AMSMATH),
    FLALIGN(myEnvName = "flalign", myContext = Context.MATH, myPackage = Package.AMSMATH),
    FLALIGN_STAR(myEnvName = "flalign*", myContext = Context.MATH, myPackage = Package.AMSMATH),
    GATHER(myEnvName = "gather", myContext = Context.MATH, myPackage = Package.AMSMATH),
    GATHER_STAR(myEnvName = "gather*", myContext = Context.MATH, myPackage = Package.AMSMATH),
    MULTLINE(myEnvName = "multline", myContext = Context.MATH, myPackage = Package.AMSMATH),
    MULTLINE_STAR(myEnvName = "multline*", myContext = Context.MATH, myPackage = Package.AMSMATH),
    SPLIT(myEnvName = "split", myContext = Context.MATH, myPackage = Package.AMSMATH),
    SPLIT_STAR(myEnvName = "split*", myContext = Context.MATH, myPackage = Package.AMSMATH),
    CASES(myEnvName = "cases", myContext = Context.MATH, myPackage = Package.AMSMATH),

    // comment
    COMMENT(myEnvName = "comment", myContext = Context.COMMENT, myPackage = Package.COMMENT);

    companion object {

        /**
         * Caches all the enum instances to increase performance.
         */
        private val lookup = HashMap<String, DefaultEnvironment>()

        init {
            for (environment in DefaultEnvironment.values()) {
                lookup.put(environment.myEnvName, environment)
            }
        }

        /**
         * Gets the [DefaultEnvironment] object that corresponds to the given Psi environment.
         */
        @JvmStatic
        fun fromPsi(latexEnvironment: LatexEnvironment): DefaultEnvironment? {
            val text: String = latexEnvironment.name()?.text ?: return null
            return get(text)
        }

        /**
         * Finds the [DefaultEnvironment] object with the given name.
         */
        @JvmStatic
        operator fun get(name: String): DefaultEnvironment? = lookup[name]
    }

    override fun getArguments() = myArguments
    override fun getEnvironmentName() = myEnvName
    override fun getInitialContents() = myInitialContents
    override fun getContext() = myContext
    override fun getDependency(): Package = myPackage
}
