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
    val unicode = hashSetOf(
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

    /**
     * Maps packages to the packages it loads.
     * This list is just there as a sort of default for those users who do not have LaTeX packages installed for example.
     */
    val packagesLoadingOtherPackages: Map<LatexLib, Set<LatexLib>> = mapOf(
        LatexLib.AMSSYMB to setOf(LatexLib.AMSFONTS),
        LatexLib.MATHTOOLS to setOf(LatexLib.AMSMATH),
        LatexLib.GRAPHICX to setOf(LatexLib.GRAPHICS),
        LatexLib.XCOLOR to setOf(LatexLib.COLOR),
        LatexLib.PDFCOMMENT to setOf(LatexLib.HYPERREF),
        LatexLib.ALGORITHM2E to setOf(LatexLib.ALGPSEUDOCODE), // Not true, but algorithm2e provides roughly the same commands
        LatexLib.NEWTXMATH to setOf(LatexLib.AMSSYMB, LatexLib.STMARYRD), // Not true, but newtxmath provides roughly the same commands
    )

    private val conflictingPackagesList = listOf(
        setOf("biblatex.sty", "natbib.sty"),
    )

    val conflictingPackageMap = buildMap {
        conflictingPackagesList.forEach { names ->
            names.forEach { name ->
                merge(name, names) { old, new -> (old + new).toSet() }
            }
        }

        // citation-style-language is not compatible with other packages, but the packages itself can still be compatible with each other.
        merge("citation-style-language.sty", setOf("babelbib.sty", "backref.sty", "bibtopic.sty", "bibunits.sty", "chapterbib.sty", "cite.sty", "citeref.sty", "inlinebib.sty", "jurabib.sty", "mcite.sty", "mciteplus.sty", "multibib.sty", "natbib.sty", "splitbib.sty")) { old, new ->
            (old + new).toSet()
        }
    }

    /**
     * Maps argument specifiers to whether they are required (true) or
     * optional (false).
     */
    val xparseParamSpecifiers = mapOf(
        'm' to true,
        'r' to true,
        'R' to true,
        'v' to true,
        'b' to true,
        'o' to false,
        'd' to false,
        'O' to false,
        'D' to false,
        's' to false,
        't' to false,
        'e' to false,
        'E' to false
    )
}