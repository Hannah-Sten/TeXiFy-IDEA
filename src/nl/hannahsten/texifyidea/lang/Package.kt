package nl.hannahsten.texifyidea.lang

/**
 * @author Hannah Schellekens
 */
open class Package @JvmOverloads constructor(
        val name: String,
        vararg val parameters: String = emptyArray()
) {

    companion object {

        // Predefined packages.
        @JvmField val DEFAULT = Package("")
        @JvmField val AMSFONTS = Package("amsfonts")
        @JvmField val AMSMATH = Package("amsmath")
        @JvmField val AMSSYMB = Package("amssymb")
        @JvmField val BIBLATEX = Package("biblatex")
        @JvmField val BOOKTABS = Package("booktabs")
        @JvmField val COMMENT = Package("comment")
        @JvmField val CLEVEREF = Package("cleveref")
        @JvmField val CSQUOTES = Package("csquotes")
        @JvmField val FONTENC = Package("fontenc")
        @JvmField val GRAPHICX = Package("graphicx")
        @JvmField val HYPERREF = Package("hyperref")
        @JvmField val IMAKEIDX = Package("imakeidx")
        @JvmField val IMPORT = Package("import")
        @JvmField val INPUTENC = Package("inputenc")
        @JvmField val LATEXSYMB = Package("latexsymb")
        @JvmField val LISTINGS = Package("listings")
        @JvmField val LUACODE = Package("luacode")
        @JvmField val MATHABX = Package("mathabx")
        @JvmField val MATHTOOLS = Package("mathtools")
        @JvmField val NATBIB = Package("natbib")
        @JvmField val SUBFILES = Package("subfiles")
        @JvmField val ULEM = Package("ulem")
        @JvmField val XPARSE = Package("xparse")
    }

    /**
     * Checks if this package is the default package ('no package').
     *
     * @return `true` when is the default package, `false` if it is any other package.
     */
    val isDefault: Boolean
        get() = equals(DEFAULT)

    /**
     * Creates a new package object with the same name and with the given parameters.
     */
    fun with(vararg parameters: String) = Package(name, *parameters)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Package) {
            return false
        }

        val aPackage = other as Package?
        return name == aPackage!!.name
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = "Package{$name}"
}