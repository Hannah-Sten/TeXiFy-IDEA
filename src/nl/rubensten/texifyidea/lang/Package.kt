package nl.rubensten.texifyidea.lang

/**
 * @author Ruben Schellekens
 */
open class Package @JvmOverloads constructor(
        val name: String,
        vararg val parameters: String = emptyArray()
) {

    companion object {

        // Predefined packages.
        @JvmField val DEFAULT = Package("")
        @JvmField val FONTENC = Package("fontenc")
        @JvmField val INPUTENC = Package("inputenc")
        @JvmField val GRAPHICX = Package("graphicx")
        @JvmField val AMSSYMB = Package("amssymb")
        @JvmField val AMSFONTS = Package("amsfonts")
        @JvmField val AMSMATH = Package("amsmath")
        @JvmField val MATHTOOLS = Package("mathtools")
        @JvmField val MATHABX = Package("mathabx")
        @JvmField val ULEM = Package("ulem")
        @JvmField val HYPERREF = Package("hyperref")
        @JvmField val LATEXSYMB = Package("latexsymb")
        @JvmField val COMMENT = Package("comment")
        @JvmField val BIBLATEX = Package("biblatex")
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