package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.file.*
import nl.hannahsten.texifyidea.inspections.latex.LatexLineBreakInspection
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.ALGORITHM2E
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.ALGPSEUDOCODE
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSFONTS
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSMATH
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSSYMB
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.BIBLATEX
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.GLOSSARIES
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.GLOSSARIESEXTRA
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.GRAPHICS
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.GRAPHICX
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.MATHTOOLS
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.NATBIB
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.PDFCOMMENT
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.XCOLOR
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.LatexRegularCommand.BIBLIOGRAPHY
import nl.hannahsten.texifyidea.lang.LatexRegularCommand.INCLUDE
import nl.hannahsten.texifyidea.util.magic.cmd
import java.util.regex.Pattern

typealias LatexPackage = LatexPackage
typealias RegexPattern = Pattern

/**
 * Magic constants are awesome!
 * todo split up
 *
 * @author Hannah Schellekens
 */
object Magic {

    /**
     * @author Hannah Schellekens
     */
    object Pattern {

        @JvmField
        val ellipsis = Regex("""(?<!\.)(\.\.\.)(?!\.)""")

        /**
         * This is the only correct way of using en dashes.
         */
        @JvmField
        val correctEnDash = RegexPattern.compile("[0-9]+--[0-9]+")!!

        /**
         * Matches the prefix of a label. Amazing comment this is right?
         */
        @JvmField
        val labelPrefix = RegexPattern.compile(".*:")!!

        /**
         * Matches the end of a sentence.
         *
         * Includes `[^.][^.]` because of abbreviations (at least in Dutch) like `s.v.p.`
         */
        @JvmField
        val sentenceEnd = RegexPattern.compile("([^.A-Z][^.A-Z][.?!;;] +[^%\\s])|(^\\. )")!!

        /**
         * Matches all interpunction that marks the end of a sentence.
         */
        @JvmField
        val sentenceSeparator = RegexPattern.compile("[.?!;;]")!!

        /**
         * Matches all sentenceSeparators at the end of a line (with or without space).
         */
        @JvmField
        val sentenceSeparatorAtLineEnd = RegexPattern.compile("$sentenceSeparator\\s*$")!!

        /**
         * Matches when a string ends with a non breaking space.
         */
        @JvmField
        val endsWithNonBreakingSpace = RegexPattern.compile("~$")!!

        /**
         * Finds all abbreviations that have at least two letters separated by comma's.
         *
         * It might be more parts, like `b.v.b.d. ` is a valid abbreviation. Likewise are `sajdflkj.asdkfj.asdf ` and
         * `i.e. `. Single period abbreviations are not being detected as they can easily be confused with two letter words
         * at the end of the sentence (also localisation...) For this there is a quickfix in [LatexLineBreakInspection].
         */
        @JvmField
        val abbreviation = RegexPattern.compile("[0-9A-Za-z.]+\\.[A-Za-z](\\.[\\s~])")!!

        /**
         * Abbreviations not detected by [Pattern.abbreviation].
         */
        val unRegexableAbbreviations = listOf(
            "et al."
        )

        /** [abbreviation]s that are missing a normal space (or a non-breaking space) */
        val abbreviationWithoutNormalSpace = RegexPattern.compile("[0-9A-Za-z.]+\\.[A-Za-z](\\.[\\s])")!!

        /**
         * Matches all comments, starting with % and ending with a newline.
         */
        val comments: RegexPattern = RegexPattern.compile("%(.*)\\n")

        /**
         * Matches leading and trailing whitespace on a string.
         */
        @JvmField
        val excessWhitespace = RegexPattern.compile("(^(\\s+).*(\\s*)\$)|(^(\\s*).*(\\s+)\$)")!!

        /**
         * Matches a non-ASCII character.
         */
        @JvmField
        val nonAscii = RegexPattern.compile("\\P{ASCII}")!!

        /**
         * Separator for multiple parameter values in one parameter.
         *
         * E.g. when you have \cite{citation1,citation2,citation3}, this pattern will match the separating
         * comma.
         */
        @JvmField
        val parameterSplit = RegexPattern.compile("\\s*,\\s*")!!

        /**
         * Matches the extension of a file name.
         */
        @JvmField
        val fileExtension = RegexPattern.compile("\\.[a-zA-Z0-9]+$")!!

        /**
         * Matches all leading whitespace.
         */
        @JvmField
        val leadingWhitespace = RegexPattern.compile("^\\s*")!!

        /**
         * Matches newlines.
         */
        @JvmField
        val newline = RegexPattern.compile("\\n")!!

        /**
         * Checks if the string is `text`, two newlines, `text`.
         */
        @JvmField
        val containsMultipleNewlines = RegexPattern.compile("[^\\n]*\\n\\n+[^\\n]*")!!

        /**
         * Matches HTML tags of the form `<tag>`, `<tag/>` and `</tag>`.
         */
        @JvmField
        val htmlTag = RegexPattern.compile("""<.*?/?>""")!!

        /**
         * Matches a LaTeX command that is the start of an \if-\fi structure.
         */
        @JvmField
        val ifCommand = RegexPattern.compile("\\\\if[a-zA-Z@]*")!!

        /**
         * Matches the begin and end commands of the cases and split environments.
         */
        @JvmField
        val casesOrSplitCommands = Regex(
            "((?=\\\\begin\\{cases})|(?<=\\\\begin\\{cases}))" +
                "|((?=\\\\end\\{cases})|(?<=\\\\end\\{cases}))" +
                "|((?=\\\\begin\\{split})|(?<=\\\\begin\\{split}))" +
                "|((?=\\\\end\\{split})|(?<=\\\\end\\{split}))"
        )
    }

    /**
     * @author Hannah Schellekens
     */
    object File {

        /**
         * All file extensions of files that can be included (and where the included files contain language that needs to be considered).
         */
        @JvmField
        val includeExtensions = hashSetOf("tex", "sty", "cls", "bib")

        val automaticExtensions = mapOf(
            INCLUDE.cmd to LatexFileType.defaultExtension,
            BIBLIOGRAPHY.cmd to BibtexFileType.defaultExtension
        )

        /**
         * All possible file types.
         */
        @JvmField
        val fileTypes = setOf(
            LatexFileType,
            StyleFileType,
            ClassFileType,
            BibtexFileType,
            TikzFileType
        )

        /**
         * All file extensions that have to be deleted when clearing auxiliary files.
         *
         * This list is the union of @generated_exts in latexmk and the defined Auxiliary files in TeXWorks, plus some additions.
         * (https://github.com/TeXworks/texworks/blob/9c8cc8b88505103cb8f43fe4105638c77c7e7303/res/resfiles/configuration/texworks-config.txt#L37).
         */
        @JvmField
        val auxiliaryFileTypes = arrayOf("aux", "bbl", "bcf", "brf", "fls", "idx", "ind", "lof", "lot", "nav", "out", "snm", "toc", "glo", "gls", "ist", "xdy")

        /**
         * All file extensions that are probably generated by LaTeX and some common packages.
         * Because any package can generated new files, this list is not complete.
         */
        // Actually run.xml should be included (latexmk) but file extensions with a dot are not found currently, see Utils#File.extension
        val generatedFileTypes = auxiliaryFileTypes + arrayOf("blg", "dvi", "fdb_latexmk", "ilg", "log", "out.ps", "pdf", "xml", "sagetex.sage", "sagetex.scmd", "sagetex.sout", "synctex", "gz", "synctex(busy)", "upa", "doctest.sage", "xdv", "glg", "glstex")

        /**
         * All bibtex keys which have a reference to a (local) file in the content.
         */
        val bibtexFileKeys = setOf("bibsource", "file")

        /**
         * Extensions of index-related files, which are generated by makeindex-like programs and should be copied next to the main file for the index/glossary packages to work.
         */
        val indexFileExtensions = setOf("ind", "glo", "ist", "xdy")

        /**
         * Extensions of files required by bib2gls
         */
        val bib2glsDependenciesExtensions = setOf("aux", "glg", "log")

        /**
         * All extensions for graphic files.
         */
        val graphicFileExtensions = setOf("eps", "pdf", "png", "jpeg", "jpg", "jbig2", "jp2")
    }

    /**
     * @author Hannah Schellekens
     */
    object Package {

        /**
         * All unicode enabling packages.
         */
        @JvmField
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
        val glossary = hashSetOf(GLOSSARIES, GLOSSARIESEXTRA).map { it.name }

        /**
         * Known conflicting packages.
         */
        val conflictingPackages = listOf(
            setOf(BIBLATEX, NATBIB)
        )

        /**
         * Maps packages to the packages it loads.
         */
        val packagesLoadingOtherPackages: Map<LatexPackage, Set<LatexPackage>> = mapOf(
            AMSSYMB to setOf(AMSFONTS),
            MATHTOOLS to setOf(AMSMATH),
            GRAPHICX to setOf(GRAPHICS),
            XCOLOR to setOf(LatexPackage.COLOR),
            PDFCOMMENT to setOf(LatexPackage.HYPERREF),
            ALGORITHM2E to setOf(ALGPSEUDOCODE), // This is not true, but loading any of these two (incompatible) packages is sufficient as they provide the same commands (roughly)
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

    /**
     * @author Hannah Schellekens
     */
    object Icon {

        /**
         * Maps file extentions to their corresponding icons.
         */
        @JvmField
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

    object Colors {
        /**
         * All commands that have a color as an argument.
         */
        @JvmField
        val takeColorCommands = LatexRegularCommand.values()
            .filter {
                it.arguments.map { it.name }.contains("color")
            }
            .map { it.command }

        /**
         * All commands that define a new color.
         */
        @JvmField
        val colorDefinitions = LatexRegularCommand.values()
            .filter { it.dependency == XCOLOR }
            .filter { it.arguments.map { it.name }.contains("name") }

        @JvmField
        val colorCommands = takeColorCommands + colorDefinitions.map { it.command }

        @JvmField
        val defaultXcolors = mapOf(
            "red" to 0xff0000,
            "green" to 0x00ff00,
            "blue" to 0x0000ff,
            "cyan" to 0x00ffff,
            "magenta" to 0xff00ff,
            "yellow" to 0xffff00,
            "black" to 0x000000,
            "gray" to 0x808080,
            "white" to 0xffffff,
            "darkgray" to 0x404040,
            "lightgray" to 0xbfbfbf,
            "brown" to 0xfb8040,
            "lime" to 0xbfff00,
            "olive" to 0x808000,
            "orange" to 0xff8000,
            "pink" to 0xffbfbf,
            "purple" to 0xbf0040,
            "teal" to 0x008080,
            "violet" to 0x800080
        )
    }
}