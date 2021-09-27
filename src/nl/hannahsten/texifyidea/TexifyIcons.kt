package nl.hannahsten.texifyidea

import com.intellij.openapi.util.IconLoader
import com.intellij.util.PlatformIcons
import java.util.*
import javax.swing.Icon

/**
 * @author Hannah Schellekens, Sten Wessel
 */
object TexifyIcons {

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val LATEX_FILE_BIG = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/latex-file-big.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017-2021 Hannah Schellekens
     */
    val LATEX_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/latex-file.svg", TexifyIcons::class.java
    ) or IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/latex-file-alt.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val LATEX_FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/latex-file_smaller.svg", TexifyIcons::class.java
    ) or IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/latex-file-alt_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2019 Hannah Schellekens
     */
    val TIKZ_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/tikz-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val TIKZ_FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/tikz-file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val PDF_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/pdf-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val PDF_FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/pdf-file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DVI_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dvi-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val DVI_FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dvi-file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val BIBLIOGRAPHY_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/bibliography-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val BIBLIOGRAPHY_FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/bibliography-file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val CLASS_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/class-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val CLASS_FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/class-file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOCUMENTED_LATEX_SOURCE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/doc-latex-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val DOCUMENTED_LATEX_SOURCE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/doc-latex-file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val STYLE_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/style-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val STYLE_FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/style-file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val TEMP_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/temp.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val SYNCTEX_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/synctex-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val SYNCTEX_FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/synctex-file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val AUX_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/aux-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val TABLE_OF_CONTENTS_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/toc-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val BBL_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/bbl-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val TEXT_FILE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/text-file.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val TEXT_FILE_SMALLER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/text-file_smaller.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val BUILD = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/build.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val BUILD_BIB = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/bib-build.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val LATEX_MODULE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/latex-module.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_LATEX = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-tex.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_COMMAND = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-cmd.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_LABEL = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-lbl.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_BIB = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-bib.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_INCLUDE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-incl.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_ENVIRONMENT = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-env.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2018 Hannah Schellekens
     */
    val DOT_CLASS = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-cls.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_NUMBER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-num.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_CHAPTER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-chap.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_PART = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-part.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_SECTION = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-sec.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_SUBSECTION = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-subsec.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_SUBSUBSECTION = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-subsubsec.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_PARAGRAPH = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-par.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val DOT_SUBPARAGRAPH = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/dot-subpar.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val FONT_BOLD = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/font-bold.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val FONT_ITALICS = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/font-italics.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val FONT_UNDERLINE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/font-underline.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val FONT_OVERLINE = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/font-overline.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val FONT_SMALLCAPS = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/font-smallcaps.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val FONT_TYPEWRITER = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/font-mono.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val FONT_STRIKETHROUGH = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/font-strike.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val FONT_SLANTED = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/font-slanted.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val SUMATRA = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/sumatra.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val WORD_COUNT = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/word-count.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val TOGGLE_STAR = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/toggle-star.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val STATS = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/stats.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2017 Hannah Schellekens
     */
    val RIGHT = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/right.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2018 Hannah Schellekens
     */
    val EQUATION_PREVIEW = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/equation-preview.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2019 Hannah Schellekens
     */
    val TIKZ_PREVIEW = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/tikz-preview.svg", TexifyIcons::class.java
    )

    /**
     * Copyright (c) 2021 Hannah Schellekens
     */
    val SYMBOLS = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/symbols.svg", TexifyIcons::class.java
    )

    // From IntelliJ
    val STRING = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/string.svg", TexifyIcons::class.java
    )

    // From IntelliJ (modified)
    val KEY_REQUIRED = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/key-required.svg", TexifyIcons::class.java
    )

    // From IntelliJ (modified)
    val KEY_USER_DEFINED = IconLoader.getIcon(
            "/nl/hannahsten/texifyidea/icons/key-user.svg", TexifyIcons::class.java
    )

    /**
     * For lookup items that have no real category.
     */
    val MISCELLANEOUS_ITEM: Icon = PlatformIcons.PACKAGE_LOCAL_ICON

    /**
     * Get the file icon object that corresponds to the given file extension.
     *
     *
     * This method ignores case.
     *
     * @param extension
     *              The extension of the file to get the icon of without a dot.
     * @param smaller
     *              `true` for a small icon, `false` for a regular sized icon.
     * @return The Icon that corresponds to the given extension.
     * @throws IllegalArgumentException
     * When `extension` is null.
     */
    fun getIconFromExtension(extension: String?, smaller: Boolean = false): Icon {
        return if (extension == null) {
            FILE
        }
        else when (extension.toLowerCase()) {
            "tex" -> if (smaller) LATEX_FILE_SMALLER else LATEX_FILE
            "bib" -> if (smaller) BIBLIOGRAPHY_FILE_SMALLER else BIBLIOGRAPHY_FILE
            "cls" -> if (smaller) CLASS_FILE_SMALLER else CLASS_FILE
            "dtx" -> if (smaller) DOCUMENTED_LATEX_SOURCE_SMALLER else DOCUMENTED_LATEX_SOURCE
            "sty" -> if (smaller) STYLE_FILE_SMALLER else STYLE_FILE
            "txt" -> if (smaller) TEXT_FILE_SMALLER else TEXT_FILE
            "tikz" -> if (smaller) TIKZ_FILE_SMALLER else TIKZ_FILE
            "log" -> if (smaller) TEXT_FILE_SMALLER else TEXT_FILE
            "pdf" -> if (smaller) PDF_FILE_SMALLER else PDF_FILE
            "synctex.gz" -> if (smaller) SYNCTEX_FILE_SMALLER else SYNCTEX_FILE
            "dvi" -> if (smaller) DVI_FILE_SMALLER else DVI_FILE
            else -> if (smaller) FILE_SMALLER else FILE
        }
    }

    /**
     * Get a variation on the tex icon if applicable.
     *
     * @param smaller
     *              `true` for a small icon, `false` for a regular sized icon.
     */
    private infix fun Icon.or(icon: Icon): Icon {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_MONTH) != 1) return this
        if (calendar.get(Calendar.MONTH) != 3) return this
        return icon
    }
}