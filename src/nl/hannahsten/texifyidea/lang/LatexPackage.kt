package nl.hannahsten.texifyidea.lang

import com.intellij.codeInsight.intention.FileModifier
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.util.files.removeFileExtension

/**
 * @author Hannah Schellekens
 */
@FileModifier.SafeTypeForPreview
open class LatexPackage @JvmOverloads constructor(
    val name: String,
    vararg val parameters: String = emptyArray(),
    /**
     * Source (dtx/sty) filename without extension.
     * Since a package can consist of multiple source/doc files, we do want
     * to track in which source file a command is defined, for example to find
     * the matching pdf file.
     */
    val fileName: String = name,
) {

    companion object {

        // Predefined packages.
        val DEFAULT = LatexPackage("")

        val ALGORITHM2E = LatexPackage("algorithm2e")
        val ALGORITHMICX = LatexPackage("algorithmicx")
        val ALGPSEUDOCODE = LatexPackage("algpseudocode")
        val AMSFONTS = LatexPackage("amsfonts")
        val AMSMATH = LatexPackage("amsmath")
        val AMSSYMB = LatexPackage("amssymb")
        val BIBLATEX = LatexPackage("biblatex")
        val BLINDTEXT = LatexPackage("blindtext")
        val BLKARRAY = LatexPackage("blkarray")
        val BM = LatexPackage("bm")
        val BOOKTABS = LatexPackage("booktabs")
        val CHAPTERBIB = LatexPackage("chapterbib")
        val CLEVEREF = LatexPackage("cleveref")
        val COLOR = LatexPackage("color")
        val COMMENT = LatexPackage("comment")
        val CSQUOTES = LatexPackage("csquotes")
        val DIFFCOEFF = LatexPackage("diffcoeff")
        val ETOOLBOX = LatexPackage("etoolbox")
        val EUROSYM = LatexPackage("eurosym")
        val FLOAT = LatexPackage("float")
        val FONTENC = LatexPackage("fontenc")
        val GAUSS = LatexPackage("gauss")
        val GLOSSARIES = LatexPackage("glossaries")
        val GLOSSARIESEXTRA = LatexPackage("glossaries-extra")
        val GRAPHICS = LatexPackage("graphics")
        val GRAPHICX = LatexPackage("graphicx")
        val HVINDEX = LatexPackage("hvindex")
        val HYPERREF = LatexPackage("hyperref")
        val IDXLAYOUT = LatexPackage("idxlayout")
        val IMAKEIDX = LatexPackage("imakeidx")
        val IMPORT = LatexPackage("import")
        val INDEX = LatexPackage("index")
        val INDEXTOOLS = LatexPackage("indextools")
        val INPUTENC = LatexPackage("inputenc")
        val LATEXSYMB = LatexPackage("latexsymb")
        val LIPSUM = LatexPackage("lipsum")
        val LISTINGS = LatexPackage("listings")
        val LUACODE = LatexPackage("luacode")
        val MAKEIDX = LatexPackage("makeidx")
        val MARVOSYM = LatexPackage("marvosym")
        val MATHABX = LatexPackage("mathabx")
        val MATHTOOLS = LatexPackage("mathtools")
        val MINTED = LatexPackage("minted")
        val MULTIND = LatexPackage("multind")
        val NATBIB = LatexPackage("natbib")
        val NEWTXMATH = LatexPackage("newtxmath")
        val NTHEOREM = LatexPackage("ntheorem")
        val NOMENCL = LatexPackage("nomencl")
        val PDFCOMMENT = LatexPackage("pdfcomment")
        val PYTHONTEX = LatexPackage("pythontex")
        val REPEATINDEX = LatexPackage("repeatindex")
        val SIUNITX = LatexPackage("siunitx")
        val SPLITIDX = LatexPackage("splitidx")
        val SPLITINDEX = LatexPackage("splitindex")
        val STMARYRD = LatexPackage("stmaryrd")
        val SUBFILES = LatexPackage("subfiles")
        val SVG = LatexPackage("svg")
        val TABULARRAY = LatexPackage("tabularray")
        val TCOLORBOX = LatexPackage("tcolorbox")
        val TEXTCOMP = LatexPackage("textcomp")
        val TIKZ = LatexPackage("tikz")
        val ULEM = LatexPackage("ulem")
        val UPGREEK = LatexPackage("upgreek")
        val VARIOREF = LatexPackage("varioref")
        val WASYSYM = LatexPackage("wasysym")
        val WIDETABLE = LatexPackage("widetable")
        val XARGS = LatexPackage("xargs")
        val XCOLOR = LatexPackage("xcolor")
        val XPARSE = LatexPackage("xparse")

        /**
         * Create package based on the source (dtx/sty) file.
         */
        fun create(sourceFile: VirtualFile): LatexPackage {
            val isLatexBase = sourceFile.parent.name == "base"
            val dependencyText =
                when (sourceFile.fileType) {
                    is StyleFileType -> sourceFile.nameWithoutExtension
                    // Shouldn't happen, but if it does, a cls file is not a package and we don't support importing it, so don't do anything
                    is ClassFileType -> ""
                    // The mapping from dtx to package names is nontrivial, we just do a guess for now
                    else -> sourceFile.parent.name
                }
            val fileName = sourceFile.name.removeFileExtension()
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
