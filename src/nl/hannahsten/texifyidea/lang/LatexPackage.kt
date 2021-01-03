package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.util.files.removeFileExtension

/**
 * @author Hannah Schellekens
 */
open class LatexPackage @JvmOverloads constructor(
    val name: String,
    vararg val parameters: String = emptyArray()
) {

    companion object {

        // Predefined packages.
        @JvmField val DEFAULT = LatexPackage("")
        @JvmField val ALGORITHM2E = LatexPackage("algorithm2e")
        @JvmField val ALGPSEUDOCODE = LatexPackage("algpseudocode")
        @JvmField val AMSFONTS = LatexPackage("amsfonts")
        @JvmField val AMSMATH = LatexPackage("amsmath")
        @JvmField val AMSSYMB = LatexPackage("amssymb")
        @JvmField val BIBLATEX = LatexPackage("biblatex")
        @JvmField val BM = LatexPackage("bm")
        @JvmField val BOOKTABS = LatexPackage("booktabs")
        @JvmField val COLOR = LatexPackage("color")
        @JvmField val COMMENT = LatexPackage("comment")
        @JvmField val CLEVEREF = LatexPackage("cleveref")
        @JvmField val CSQUOTES = LatexPackage("csquotes")
        @JvmField val FONTENC = LatexPackage("fontenc")
        @JvmField val GAUSS = LatexPackage("gauss")
        @JvmField val GLOSSARIES = LatexPackage("glossaries")
        @JvmField val GLOSSARIESEXTRA = LatexPackage("glossaries-extra")
        @JvmField val GRAPHICS = LatexPackage("graphics")
        @JvmField val GRAPHICX = LatexPackage("graphicx")
        @JvmField val HYPERREF = LatexPackage("hyperref")
        @JvmField val IMAKEIDX = LatexPackage("imakeidx")
        @JvmField val IMPORT = LatexPackage("import")
        @JvmField val INPUTENC = LatexPackage("inputenc")
        @JvmField val LATEXSYMB = LatexPackage("latexsymb")
        @JvmField val LISTINGS = LatexPackage("listings")
        @JvmField val LUACODE = LatexPackage("luacode")
        @JvmField val MATHABX = LatexPackage("mathabx")
        @JvmField val MATHTOOLS = LatexPackage("mathtools")
        @JvmField val NATBIB = LatexPackage("natbib")
        @JvmField val PDFCOMMENT = LatexPackage("pdfcomment")
        @JvmField val PYTHONTEX = LatexPackage("pythontex")
        @JvmField val SIUNITX = LatexPackage("siunitx")
        @JvmField val SUBFILES = LatexPackage("subfiles")
        @JvmField val TIKZ = LatexPackage("tikz")
        @JvmField val ULEM = LatexPackage("ulem")
        @JvmField val XCOLOR = LatexPackage("xcolor")
        @JvmField val XPARSE = LatexPackage("xparse")

        /**
         * Create package based on the source (dtx) file name.
         */
        fun create(sourceFileName: String): LatexPackage {
            val dependencyText = sourceFileName.removeFileExtension()
            return if (dependencyText.isBlank() || dependencyText.startsWith("lt")) LatexPackage.DEFAULT else LatexPackage(dependencyText)
        }
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
    fun with(vararg parameters: String) = LatexPackage(name, *parameters)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is LatexPackage) {
            return false
        }

        val aPackage = other as LatexPackage?
        return name == aPackage!!.name
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = "Package{$name}"
}