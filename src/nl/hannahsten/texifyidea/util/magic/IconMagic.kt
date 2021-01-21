package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.TexifyIcons

object IconMagic {

    /**
     * Maps file extentions to their corresponding icons.
     */
    val fileIcons = mapOf(
            "pdf" to TexifyIcons.PDF_FILE,
            "dvi" to TexifyIcons.DVI_FILE,
            "synctex.gz" to TexifyIcons.SYNCTEX_FILE,
            "bbl" to TexifyIcons.BBL_FILE,
            "aux" to TexifyIcons.AUX_FILE,
            "tmp" to TexifyIcons.TEMP_FILE,
            "dtx" to TexifyIcons.DOCUMENTED_LATEX_SOURCE,
            "bib" to TexifyIcons.BIBLIOGRAPHY_FILE,
            "toc" to TexifyIcons.TABLE_OF_CONTENTS_FILE,
            "tikz" to TexifyIcons.TIKZ_FILE
    )
}