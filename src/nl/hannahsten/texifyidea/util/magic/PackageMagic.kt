package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.lang.LatexPackage

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
            "makeidx", "multind", "index", "splitidx", "splitindex", "imakeidx", "hvindex", "idxlayout", "repeatindex", "indextools"
    )

    /**
     * Packages which provide a glossary.
     */
    val glossary = hashSetOf(LatexPackage.GLOSSARIES, LatexPackage.GLOSSARIESEXTRA).map { it.name }

    /**
     * Known conflicting packages.
     */
    val conflictingPackages = listOf(
            setOf(LatexPackage.BIBLATEX, LatexPackage.NATBIB)
    )

    /**
     * Maps packages to the packages it loads. // todo replace by file based index
     */
    val packagesLoadingOtherPackages: Map<LatexPackage, Set<LatexPackage>> = mapOf(
            LatexPackage.AMSSYMB to setOf(LatexPackage.AMSFONTS),
            LatexPackage.MATHTOOLS to setOf(LatexPackage.AMSMATH),
            LatexPackage.GRAPHICX to setOf(LatexPackage.GRAPHICS),
            LatexPackage.XCOLOR to setOf(LatexPackage.COLOR),
            LatexPackage.PDFCOMMENT to setOf(LatexPackage.HYPERREF),
            LatexPackage.ALGORITHM2E to setOf(LatexPackage.ALGPSEUDOCODE), // This is not true, but loading any of these two (incompatible) packages is sufficient as they provide the same commands (roughly)
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