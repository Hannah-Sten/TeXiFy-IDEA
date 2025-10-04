package nl.hannahsten.texifyidea.lang

/**
 * Represents a LaTeX library, which can be a package, a class, the base LaTeX installation or nothing but custom code.
 */
data class LatexLib(
    /**
     * The name without the suffix.
     */
    val name: String,
    val type: LibType
) {
    val isPackageFile: Boolean
        get() = type == LibType.PACKAGE

    val isClassFile: Boolean
        get() = type == LibType.CLASS

    val requiresImport: Boolean
        get() = isPackageFile || isClassFile

    val isDefault: Boolean
        get() = type == LibType.BASE

    val isCustom: Boolean
        get() = type == LibType.CUSTOM

    fun asPackageName(): String? {
        return if (isPackageFile) name else null
    }

    fun asClassName(): String? {
        return if (isClassFile) name else null
    }

    /**
     * Gets the file name with suffix, or null if this is not a package or class.
     */
    fun toFileName(): String? = when (type) {
        LibType.PACKAGE -> "$name.sty"
        LibType.CLASS -> "$name.cls"
        LibType.BASE -> null
        LibType.CUSTOM -> null
    }

    /**
     * Returns a display string for this library.
     *
     * @param withParan Whether to include parentheses and suffix in the display string (but not for base or custom).
     */
    fun displayString(withParan: Boolean = false): String {
        return when(type) {
            LibType.PACKAGE -> if(withParan) "($name.sty)" else "$name.sty"
            LibType.CLASS -> if(withParan) "($name.cls)" else "$name.cls"
            LibType.BASE -> "(base)"
            LibType.CUSTOM -> ""
        }
    }

    override fun toString(): String {
        return displayString(false)
    }

    enum class LibType {
        PACKAGE,
        CLASS,
        BASE,
        CUSTOM
    }

    companion object {
        /**
         * Represents custom code, no package or class.
         */
        val CUSTOM = LatexLib("", LibType.CUSTOM)

        /**
         * Represents the base LaTeX installation, no package or class.
         */
        val BASE = LatexLib("(base)", LibType.BASE) // Represents the base package

        @Suppress("FunctionName") // package is a keyword in Kotlin
        fun Package(name: String): LatexLib {
            return if (name.isEmpty()) BASE else LatexLib(name, LibType.PACKAGE)
        }

        @Suppress("FunctionName")
        fun Class(name: String): LatexLib {
            return if (name.isEmpty()) BASE else LatexLib(name, LibType.CLASS)
        }

        fun fromFileName(fileName: String): LatexLib {
            if (fileName.isEmpty()) return BASE
            when {
                fileName.endsWith(".sty") -> return LatexLib(fileName.removeSuffix(".sty"), LibType.PACKAGE)
                fileName.endsWith(".cls") -> return LatexLib(fileName.removeSuffix(".cls"), LibType.CLASS)
            }
            return CUSTOM
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

        val EXAM = Class("exam")
    }
}

fun LatexLib.toLatexPackage(): LatexPackage? {
    return if (isPackageFile) LatexPackage(name) else null
}