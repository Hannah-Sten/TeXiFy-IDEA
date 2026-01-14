package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.HVINDEX
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.IDXLAYOUT
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.IMAKEIDX
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.INDEX
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.INDEXTOOLS
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.MAKEIDX
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.MULTIND
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.NOMENCL
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.REPEATINDEX
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.SPLITIDX
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.SPLITINDEX

object PackageMagic {

    /**
     * All unicode enabling packages.
     */
    val unicode = hashSetOf(
        LatexPackage.INPUTENC.with("utf8"),
        LatexPackage.FONTENC.with("T1")
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
    val glossary = hashSetOf(LatexPackage.GLOSSARIES, LatexPackage.GLOSSARIESEXTRA)

    val glossaryNames = glossary.map { it.name }.toSet()

    /**
     * Maps packages to the packages it loads.
     * This list is just there as a sort of default for those users who do not have LaTeX packages installed for example.
     */
    val packagesLoadingOtherPackages: Map<LatexPackage, Set<LatexPackage>> = mapOf(
        LatexPackage.AMSSYMB to setOf(LatexPackage.AMSFONTS),
        LatexPackage.MATHTOOLS to setOf(LatexPackage.AMSMATH),
        LatexPackage.GRAPHICX to setOf(LatexPackage.GRAPHICS),
        LatexPackage.XCOLOR to setOf(LatexPackage.COLOR),
        LatexPackage.PDFCOMMENT to setOf(LatexPackage.HYPERREF),
        LatexPackage.ALGORITHM2E to setOf(LatexPackage.ALGPSEUDOCODE), // Not true, but algorithm2e provides roughly the same commands
        LatexPackage.NEWTXMATH to setOf(LatexPackage.AMSSYMB, LatexPackage.STMARYRD), // Not true, but newtxmath provides roughly the same commands
    )

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