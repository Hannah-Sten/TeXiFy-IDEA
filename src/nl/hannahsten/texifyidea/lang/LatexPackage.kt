package nl.hannahsten.texifyidea.lang

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.util.files.removeFileExtension

/**
 * @author Hannah Schellekens
 */
open class LatexPackage @JvmOverloads constructor(
    val name: String,
    vararg val parameters: String = emptyArray(),
    /**
     * Source (dtx) filename without extension.
     * Since a package can consist of multiple source/doc files, we do want
     * to track in which source file a command is defined, for example to find
     * the matching pdf file.
     */
    val fileName: String = name,
) {

    companion object {

        // Predefined packages.
        @JvmField val DEFAULT = LatexPackage("")
        @JvmField val ALGORITHM2E = LatexPackage("algorithm2e")
        @JvmField val ALGORITHMICX = LatexPackage("algorithmicx")
        @JvmField val ALGPSEUDOCODE = LatexPackage("algpseudocode")
        @JvmField val AMSFONTS = LatexPackage("amsfonts")
        @JvmField val AMSMATH = LatexPackage("amsmath")
        @JvmField val AMSSYMB = LatexPackage("amssymb")
        @JvmField val BIBLATEX = LatexPackage("biblatex")
        @JvmField val BLINDTEXT = LatexPackage("blindtext")
        @JvmField val BM = LatexPackage("bm")
        @JvmField val BOOKTABS = LatexPackage("booktabs")
        @JvmField val COLOR = LatexPackage("color")
        @JvmField val COMMENT = LatexPackage("comment")
        @JvmField val CLEVEREF = LatexPackage("cleveref")
        @JvmField val CSQUOTES = LatexPackage("csquotes")
        @JvmField val EUROSYM = LatexPackage("eurosym")
        @JvmField val FLOAT = LatexPackage("float")
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
        @JvmField val LIPSUM = LatexPackage("lipsum")
        @JvmField val LISTINGS = LatexPackage("listings")
        @JvmField val LUACODE = LatexPackage("luacode")
        @JvmField val MARVOSYM = LatexPackage("marvosym")
        @JvmField val MATHABX = LatexPackage("mathabx")
        @JvmField val MATHTOOLS = LatexPackage("mathtools")
        @JvmField val NATBIB = LatexPackage("natbib")
        @JvmField val PDFCOMMENT = LatexPackage("pdfcomment")
        @JvmField val PYTHONTEX = LatexPackage("pythontex")
        @JvmField val SIUNITX = LatexPackage("siunitx")
        @JvmField val SUBFILES = LatexPackage("subfiles")
        @JvmField val STMARYRD = LatexPackage("stmaryrd")
        @JvmField val TEXTCOMP = LatexPackage("textcomp")
        @JvmField val TIKZ = LatexPackage("tikz")
        @JvmField val ULEM = LatexPackage("ulem")
        @JvmField val WASYSYM = LatexPackage("wasysym")
        @JvmField val XCOLOR = LatexPackage("xcolor")
        @JvmField val XPARSE = LatexPackage("xparse")

        /**
         * Create package based on the source (dtx) file.
         */
        fun create(sourceFileName: VirtualFile): LatexPackage {
            val isLatexBase = sourceFileName.parent.name == "base"
            val dependencyText = sourceFileName.parent.name
            val fileName = sourceFileName.name.removeFileExtension()
            return if (isLatexBase) LatexPackage("", fileName = fileName) else LatexPackage(dependencyText, fileName = fileName)
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
     * The name of the package, or the empty string when this is the default package.
     */
    val displayString = if (isDefault) "" else name

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