package nl.hannahsten.texifyidea.lang

/**
 * Represents a LaTeX library, which can be a package or class file.
 *
 *
 */
@JvmInline
value class LatexLib(val name: String) {
    val isPackageFile: Boolean
        get() = name.endsWith(".sty")

    val isClassFile: Boolean
        get() = name.endsWith(".cls")

    val requiresImport: Boolean
        get() = isPackageFile || isClassFile

    val isDefault: Boolean
        get() = this == BASE

    val isCustom: Boolean
        get() = this == CUSTOM

    fun toPackageName(): String? {
        return if (isPackageFile) name.substringBefore('.') else null
    }

    val displayString: String
        get() = if (isDefault) "(base)" else if (isCustom) "" else name

    override fun toString(): String {
        return name
    }

    companion object {
        val CUSTOM = LatexLib("")

        val BASE = LatexLib("(base)") // Represents the base package

        @Suppress("FunctionName") // package is a keyword in Kotlin
        fun Package(name: String): LatexLib {
            return if (name.isEmpty()) BASE else LatexLib("$name.sty")
        }

        @Suppress("FunctionName")
        fun Class(name: String): LatexLib {
            return if (name.isEmpty()) BASE else LatexLib("$name.cls")
        }

        fun fromFileName(fileName: String): LatexLib {
            return if (fileName.isEmpty()) BASE else LatexLib(fileName)
        }

        val ACRONYM = Package("acronym")
        val ADDTOLUATEXPATH = Package("addtoluatexpath")
        val ALGORITHM2E = Package("algorithm2e")
        val ALGORITHMICX = Package("algorithmicx")
        val ALGPSEUDOCODE = Package("algpseudocode")
        val AMSFONTS = Package("amsfonts")
        val AMSMATH = Package("amsmath")
        val AMSTHM = Package("amsthm")
        val AMSSYMB = Package("amssymb")
        val BIBLATEX = Package("biblatex")
        val BLINDTEXT = Package("blindtext")
        val BLKARRAY = Package("blkarray")
        val BM = Package("bm")
        val BOOKTABS = Package("booktabs")
        val CHAPTERBIB = Package("chapterbib")
        val CLEVEREF = Package("cleveref")
        val COLOR = Package("color")
        val COMMENT = Package("comment")
        val CSQUOTES = Package("csquotes")
        val DIFFCOEFF = Package("diffcoeff")
        val ETOOLBOX = Package("etoolbox")
        val EUROSYM = Package("eurosym")
        val FLOAT = Package("float")
        val FONTENC = Package("fontenc")
        val FONTSPEC = Package("fontspec")
        val GAUSS = Package("gauss")
        val GLOSSARIES = Package("glossaries")
        val GLOSSARIESEXTRA = Package("glossaries-extra")
        val GRAPHICS = Package("graphics")
        val GRAPHICX = Package("graphicx")
        val HVINDEX = Package("hvindex")
        val HYPERREF = Package("hyperref")
        val IDXLAYOUT = Package("idxlayout")
        val IMAKEIDX = Package("imakeidx")
        val IMPORT = Package("import")
        val INDEX = Package("index")
        val INDEXTOOLS = Package("indextools")
        val INPUTENC = Package("inputenc")
        val LATEXSYMB = Package("latexsymb")
        val LIPSUM = Package("lipsum")
        val LISTINGS = Package("listings")
        val LUACODE = Package("luacode")
        val MAKEIDX = Package("makeidx")
        val MARVOSYM = Package("marvosym")
        val MATHABX = Package("mathabx")
        val MATHTOOLS = Package("mathtools")
        val MINTED = Package("minted")
        val MULTIND = Package("multind")
        val NATBIB = Package("natbib")
        val NEWTXMATH = Package("newtxmath")
        val NTHEOREM = Package("ntheorem")
        val NOMENCL = Package("nomencl")
        val PDFCOMMENT = Package("pdfcomment")
        val PYTHONTEX = Package("pythontex")
        val REPEATINDEX = Package("repeatindex")
        val SIUNITX = Package("siunitx")
        val SPLITIDX = Package("splitidx")
        val SPLITINDEX = Package("splitindex")
        val STMARYRD = Package("stmaryrd")
        val STANDALONE = Package("standalone")
        val SUBFILES = Package("subfiles")
        val SVG = Package("svg")
        val TABULARRAY = Package("tabularray")
        val TCOLORBOX = Package("tcolorbox")
        val TEXTCOMP = Package("textcomp")
        val TIKZ = Package("tikz")
        val TIKZIT = Package("tikzit") // not in ctan
        val TODONOTES = Package("todonotes")
        val ULEM = Package("ulem")
        val UPGREEK = Package("upgreek")
        val VARIOREF = Package("varioref")
        val WASYSYM = Package("wasysym")
        val WIDETABLE = Package("widetable")
        val XARGS = Package("xargs")
        val XCOLOR = Package("xcolor")
        val XPARSE = Package("xparse")
    }
}

fun LatexLib.toLatexPackage(): LatexPackage? {
    if (!isPackageFile) return null
    return LatexPackage(name.substringBefore('.'))
}