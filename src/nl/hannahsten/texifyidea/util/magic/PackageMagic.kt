package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.HVINDEX
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.IDXLAYOUT
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.IMAKEIDX
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.INDEX
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.INDEXTOOLS
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.MAKEIDX
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.MULTIND
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.NOMENCL
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.REPEATINDEX
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.SPLITIDX
import nl.hannahsten.texifyidea.lang.LatexLib.Companion.SPLITINDEX

object PackageMagic {

    /**
     * All unicode enabling packages.
     */
    // Keep deterministic order for quick-fix insertion and tests.
    val unicode = linkedSetOf(
        LatexLib.INPUTENC, // Actually only with utf8 option
        LatexLib.FONTENC, // Actually only with T1 option
    )

    /**
     * All known packages which provide an index.
     */
    val index = hashSetOf(
        MAKEIDX, MULTIND, INDEX, SPLITIDX, SPLITINDEX, IMAKEIDX, HVINDEX, IDXLAYOUT, REPEATINDEX, INDEXTOOLS, NOMENCL
    )

    val indexNames = index.map { it.name }.toSet()

    /**
     * Packages which provide a glossary.
     */
    val glossary = hashSetOf(LatexLib.GLOSSARIES, LatexLib.GLOSSARIESEXTRA)

    val glossaryNames = glossary.map { it.name }.toSet()

    // Some packages load other packages only when some package option is provided
    val packagesLoadedWithOptions = mapOf(
        LatexLib.BIBLATEX to mapOf("natbib" to LatexLib.NATBIB) // Not directly true, but biblatex will provide natbib-like commands
    )

    val conflictingPackageMap = buildMap {
        // citation-style-language is not compatible with other packages, but the packages itself can still be compatible with each other.
        merge("citation-style-language.sty", setOf("babelbib.sty", "backref.sty", "bibtopic.sty", "bibunits.sty", "chapterbib.sty", "cite.sty", "citeref.sty", "inlinebib.sty", "jurabib.sty", "mcite.sty", "mciteplus.sty", "multibib.sty", "natbib.sty", "splitbib.sty")) { old, new ->
            (old + new).toSet()
        }
        merge("biblatex.sty", setOf("babelbib.sty", "backref.sty", "bibtopic.sty", "bibunits.sty", "chapterbib.sty", "cite.sty", "citeref.sty", "inlinebib.sty", "jurabib.sty", "mcite.sty", "mciteplus.sty", "multibib.sty", "natbib.sty", "splitbib.sty", "titlesec.sty", "ucs.sty", "etextools.sty")) { old, new ->
            (old + new).toSet()
        }
    }

    /**
     * Packages that generally require a Unicode-capable engine (LuaLaTeX/XeLaTeX) over pdfLaTeX.
     */
    val unicodePreferredEnginesPackages = setOf(
        LatexLib.CITATION_STYLE_LANGUAGE,
        LatexLib.FONTSPEC,
        LatexLib.Package("unicode-math"),
        LatexLib.Package("polyglossia"),
    )
}
