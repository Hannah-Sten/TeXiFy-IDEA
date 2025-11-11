package nl.hannahsten.texifyidea

import com.intellij.openapi.util.IconLoader
import com.intellij.util.PlatformIcons
import java.util.*
import javax.swing.Icon

/**
 * @author Hannah Schellekens, Sten Wessel
 */
object TexifyIcons {

    private val TEXT_FILE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/text-file.svg", TexifyIcons::class.java
    )

    @JvmField
    val LATEX_FILE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/latex-file.svg", TexifyIcons::class.java
    ) or IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/latex-file-alt.svg", TexifyIcons::class.java
    )

    @JvmField
    val TIKZ_FILE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/tikz-file.svg", TexifyIcons::class.java
    )

    @JvmField
    val PDF_FILE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/pdf-file.svg", TexifyIcons::class.java
    )

    @JvmField
    val DVI_FILE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dvi-file.svg", TexifyIcons::class.java
    )

    @JvmField
    val BIBLIOGRAPHY_FILE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/bibliography-file.svg", TexifyIcons::class.java
    )

    @JvmField
    val CLASS_FILE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/class-file.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOCUMENTED_LATEX_SOURCE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/doc-latex-file.svg", TexifyIcons::class.java
    )

    @JvmField
    val STYLE_FILE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/style-file.svg", TexifyIcons::class.java
    )

    @JvmField
    val FILE = TEXT_FILE

    @JvmField
    val SYNCTEX_FILE = TEXT_FILE

    @JvmField
    val AUX_FILE = TEXT_FILE

    @JvmField
    val TABLE_OF_CONTENTS_FILE = TEXT_FILE

    @JvmField
    val BBL_FILE = TEXT_FILE

    @JvmField
    val BUILD = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/build.svg", TexifyIcons::class.java
    )

    @JvmField
    val BUILD_BIB = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/bib-build.svg", TexifyIcons::class.java
    )

    @JvmField
    val LATEX_MODULE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/latex-module.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_LATEX = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-tex.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_COMMAND = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-cmd.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_LABEL = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-lbl.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_BIB = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-bib.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_INCLUDE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-incl.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_ENVIRONMENT = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-env.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_CLASS = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-cls.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_NUMBER = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-num.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_CHAPTER = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-chap.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_PART = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-part.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_SECTION = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-sec.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_SUBSECTION = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-subsec.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_SUBSUBSECTION = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-subsubsec.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_PARAGRAPH = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-par.svg", TexifyIcons::class.java
    )

    @JvmField
    val DOT_SUBPARAGRAPH = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/dot-subpar.svg", TexifyIcons::class.java
    )

    @JvmField
    val FONT_BOLD = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/font-bold.svg", TexifyIcons::class.java
    )

    @JvmField
    val FONT_ITALICS = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/font-italics.svg", TexifyIcons::class.java
    )

    @JvmField
    val FONT_UNDERLINE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/font-underline.svg", TexifyIcons::class.java
    )

    @JvmField
    val FONT_OVERLINE = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/font-overline.svg", TexifyIcons::class.java
    )

    @JvmField
    val FONT_SMALLCAPS = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/font-smallcaps.svg", TexifyIcons::class.java
    )

    @JvmField
    val FONT_TYPEWRITER = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/font-mono.svg", TexifyIcons::class.java
    )

    @JvmField
    val FONT_STRIKETHROUGH = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/font-strike.svg", TexifyIcons::class.java
    )

    @JvmField
    val FONT_SLANTED = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/font-slanted.svg", TexifyIcons::class.java
    )

    @JvmField
    val SUMATRA = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/sumatra.svg", TexifyIcons::class.java
    )

    @JvmField
    val WORD_COUNT = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/word-count.svg", TexifyIcons::class.java
    )

    @JvmField
    val TOGGLE_STAR = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/toggle-star.svg", TexifyIcons::class.java
    )

    @JvmField
    val STATS = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/stats.svg", TexifyIcons::class.java
    )

    @JvmField
    val RIGHT = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/right.svg", TexifyIcons::class.java
    )

    @JvmField
    val EQUATION_PREVIEW = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/equation-preview.svg", TexifyIcons::class.java
    )

    @JvmField
    val TIKZ_PREVIEW = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/tikz-preview.svg", TexifyIcons::class.java
    )

    @JvmField
    val SYMBOLS = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/symbols.svg", TexifyIcons::class.java
    )

    @JvmField
    val STRING = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/string.svg", TexifyIcons::class.java
    )

    @JvmField
    val KEY_REQUIRED = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/key-required.svg", TexifyIcons::class.java
    )

    @JvmField
    val KEY_USER_DEFINED = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/key-user.svg", TexifyIcons::class.java
    )

    @JvmField
    val INSPECTION = IconLoader.getIcon(
        "/nl/hannahsten/texifyidea/icons/inspections-eye.svg", TexifyIcons::class.java
    )

    /**
     * For lookup items that have no real category.
     */
    @JvmField
    val MISCELLANEOUS_ITEM: Icon = PlatformIcons.PACKAGE_LOCAL_ICON

    /**
     * Get the file icon object that corresponds to the given file extension.
     *
     *
     * This method ignores case.
     *
     * @param extension The extension of the file to get the icon of without a dot.
     * @param default Use this if you want to get an icon also if the extension is not known to TeXiFy
     *
     * @return The Icon that corresponds to the given extension.
     * @throws IllegalArgumentException
     * When `extension` is null.
     */
    fun getIconFromExtension(extension: String?, default: Icon? = null): Icon? {
        if (extension == null) return default
        return when (extension.lowercase(Locale.getDefault())) {
            "tex" -> LATEX_FILE
            "bib" -> BIBLIOGRAPHY_FILE
            "cls" -> CLASS_FILE
            "dtx" -> DOCUMENTED_LATEX_SOURCE
            "sty" -> STYLE_FILE
            "tikz" -> TIKZ_FILE
            "log" -> TEXT_FILE
            "pdf" -> PDF_FILE
            "synctex.gz" -> SYNCTEX_FILE
            "dvi" -> DVI_FILE
            else -> default
        }
    }

    /**
     * Get a variation on the tex icon if applicable.
     */
    private infix fun Icon.or(icon: Icon): Icon {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_MONTH) != 1) return this
        if (calendar.get(Calendar.MONTH) != 3) return this
        return icon
    }
}