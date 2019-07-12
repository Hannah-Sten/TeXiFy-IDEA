package nl.hannahsten.texifyidea.util

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.file.*
import nl.hannahsten.texifyidea.inspections.latex.LatexLineBreakInspection
import nl.hannahsten.texifyidea.lang.Package
import java.awt.Color
import java.util.regex.Pattern

typealias LatexPackage = Package
typealias RegexPattern = Pattern

/**
 * Magic constants are awesome!
 *
 * @author Hannah Schellekens
 */
object Magic {

    /**
     * @author Hannah Schellekens
     */
    object General {

        const val pathPackageRoot = "/nl/hannahsten/texifyidea"
        @JvmField val emptyStringArray = arrayOfNulls<String>(0)
        @JvmField val emptyPsiElementArray = arrayOfNulls<PsiElement>(0)
        @JvmField val noQuickFix: LocalQuickFix? = null
    }

    /**
     * @author Hannah Schellekens
     */
    object Typography {

        /**
         * Matches each (supported) opening brace to it's opposite close brace.
         */
        @JvmField val braceOpposites = mapOfVarargs(
                "(", ")",
                "[", "]",
                "\\{", "\\}",
                "<", ">",
                "|", "|",
                "\\|", "\\|"
        )
    }

    /**
     * @author Hannah Schellekens
     */
    object Environment {

        @JvmField val listingEnvironments = hashSetOf("itemize", "enumerate", "description")

        /**
         * Map that maps all environments that are expected to have a label to the label prefix they have by convention.
         *
         * environment name `=>` label prefix without colon
         */
        val labeled = mapOfVarargs(
                "figure", "fig",
                "table", "tab",
                "tabular", "tab",
                "equation", "eq",
                "algorithm", "alg"
        )

        /**
         * Environments that introduce figures
         */
        val figures = hashSetOf("figure")
    }

    /**
     * @author Hannah Schellekens
     */
    object Command {

        /**
         * LaTeX commands that make the text take up more vertical space.
         */
        @JvmField val high = hashSetOf(
                "\\frac", "\\dfrac", "\\sqrt", "\\sum", "\\int", "\\iint", "\\iiint", "\\iiiint",
                "\\prod", "\\bigcup", "\\bigcap", "\\bigsqcup", "\\bigsqcap"
        )

        /**
         * Map that maps all commands that are expected to have a label to the label prefix they have by convention.
         *
         * command name `=>` label prefix without colon
         */
        @JvmField val labeled = mapOfVarargs(
                "\\chapter", "ch",
                "\\section", "sec",
                "\\subsection", "subsec",
                "\\item", "itm"
        )

        /**
         * All commands that represent a reference to a label.
         */
        @JvmField val labelReference = hashSetOf(
                "\\ref", "\\eqref", "\\nameref", "\\autoref",
                "\\fullref", "\\pageref", "\\vref", "\\Autoref", "\\cref",
                "\\labelcref", "\\cpageref"
        )

        /**
         * All commands that represent a reference to a bibiography entry/item.
         */
        @JvmField val bibliographyReference = hashSetOf(
                "\\cite", "\\nocite", "\\citep", "\\citep*", "\\citet", "\\citet*", "\\Citep",
                "\\Citep*", "\\Citet", "\\Citet*", "\\citealp", "\\citealp*", "\\citealt", "\\citealt*",
                "\\Citealp", "\\Citealp*", "\\Citealt", "\\Citealt*", "\\citeauthor", "\\citeauthor*",
                "\\Citeauthor", "\\Citeauthor*", "\\citeyear", "\\citeyearpar"
        )

        /**
         * All commands that represend some kind of reference (think \ref and \cite).
         */
        @JvmField val reference = labelReference + bibliographyReference

        /**
         * All commands that define labels.
         */
        @JvmField val labelDefinition = setOf("\\label")

        /**
         * All commands that define bibliography items.
         */
        @JvmField val bibliographyItems = setOf("\\bibitem")

        /**
         * All label definition commands.
         */
        @JvmField val labels = setOf("\\label")

        /**
         * All math operators without a leading slash.
         */
        @JvmField val slashlessMathOperators = hashSetOf(
                "arccos", "arcsin", "arctan", "arg", "cos", "cosh", "cot", "coth", "csc",
                "deg", "det", "dim", "exp", "gcd", "hom", "inf", "ker", "lg", "lim", "liminf", "limsup",
                "ln", "log", "max", "min", "Pr", "sec", "sin", "sinh", "sup", "tan", "tanh"
        )

        /**
         * All commands that define new commands.
         */
        @JvmField val commandDefinitions = hashSetOf(
                "\\newcommand",
                "\\let",
                "\\def",
                "\\DeclareMathOperator",
                "\\newif",
                "\\NewDocumentCommand",
                "\\ProvideDocumentCommand",
                "\\DeclareDocumentCommand"
        )

        /**
         * All commands that define new documentclasses.
         */
        @JvmField val classDefinitions = hashSetOf("\\ProvidesClass")

        /**
         * All commands that define new packages.
         */
        @JvmField val packageDefinitions = hashSetOf("\\ProvidesPackage")

        /**
         * All commands that define new environments.
         */
        @JvmField val environmentDefinitions = hashSetOf(
                "\\newenvironment",
                "\\newtheorem",
                "\\NewDocumentEnvironment",
                "\\ProvideDocumentEnvironment",
                "\\DeclareDocumentEnvironment"
        )

        /**
         * All commands that define stuff like classes, environments, and definitions.
         */
        @JvmField val definitions = commandDefinitions + classDefinitions + packageDefinitions + environmentDefinitions

        /**
         * All commands that are able to redefine other commands.
         */
        @JvmField val redefinitions = hashSetOf("\\renewcommand", "\\def", "\\let", "\\renewenvironment")

        /**
         * All commands that include other files.
         */
        @JvmField val includes = hashSetOf(
                "\\includeonly", "\\include", "\\input", "\\bibliography", "\\addbibresource", "\\RequirePackage", "\\usepackage"
        )

        /**
         * Commands for which TeXiFy-IDEA has custom behaviour.
         */
        @JvmField val fragile = hashSetOf(
                "\\addtocounter", "\\begin", "\\chapter", "\\def", "\\documentclass", "\\end",
                "\\include", "\\includeonly", "\\input", "\\label", "\\let", "\\newcommand",
                "\\overline", "\\paragraph", "\\part", "\\renewcommand", "\\section", "\\setcounter",
                "\\sout", "\\subparagraph", "\\subsection", "\\subsubsection", "\\textbf",
                "\\textit", "\\textsc", "\\textsl", "\\texttt", "\\underline", "\\[", "\\]",
                "\\newenvironment", "\\bibitem",
                "\\NewDocumentCommand",
                "\\ProvideDocumentCommand",
                "\\DeclareDocumentCommand",
                "\\NewDocumentEnvironment",
                "\\ProvideDocumentEnvironment",
                "\\DeclareDocumentEnvironment"
        )

        /**
         * Commands that should not have the given file extensions.
         */
        @JvmField val illegalExtensions = mapOf(
                "\\include" to listOf(".tex"),
                "\\bibliography" to listOf(".bib")
        )

        /**
         * Commands that should have the given file extensions.
         */
        @JvmField val requiredExtensions = mapOf(
                "\\addbibresource" to listOf("bib")
        )

        /**
         * Extensions that should only be scanned for the provided include commands.
         */
        @JvmField val includeOnlyExtensions = mapOf(
                "\\include" to hashSetOf("tex"),
                "\\includeonly" to hashSetOf("tex"),
                "\\bibliography" to hashSetOf("bib"),
                "\\addbibresource" to hashSetOf("bib"),
                "\\RequirePackage" to hashSetOf("sty"),
                "\\usepackage" to hashSetOf("sty")
        )

        /**
         * All commands that end if.
         */
        @JvmField val endIfs = hashSetOf("\\fi")

        /**
         * All commands that at first glance look like \if-esque commands, but that actually aren't.
         */
        @JvmField val ignoredIfs = hashSetOf("\\newif", "\\iff")

        /**
         * List of all TeX style primitives.
         */
        @JvmField val stylePrimitives = listOf(
                "\\rm", "\\sf", "\\tt", "\\it", "\\sl", "\\sc", "\\bf"
        )

        /**
         * The LaTeX counterparts of all [stylePrimitives] commands where %s is the content.
         */
        @JvmField val stylePrimitveReplacements = listOf(
                "\\textrm{%s}", "\\textsf{%s}", "\\texttt{%s}", "\\textit{%s}",
                "\\textsl{%s}", "\\textsc{%s}", "\\textbf{%s}"
        )

        /**
         * All commands that mark some kind of section.
         */
        @JvmField val sectionMarkers = listOf(
                "\\part", "\\chapter",
                "\\section", "\\subsection", "\\subsubsection",
                "\\paragraph", "\\subparagraph"
        )

        /**
         * The colours that each section separator has.
         */
        @JvmField val sectionSeparatorColors = mapOf(
                "\\part" to Color(152, 152, 152),
                "\\chapter" to Color(172, 172, 172),
                "\\section" to Color(182, 182, 182),
                "\\subsection" to Color(202, 202, 202),
                "\\subsubsection" to Color(212, 212, 212),
                "\\paragraph" to Color(222, 222, 222),
                "\\subparagraph" to Color(232, 232, 232)
        )
    }

    /**
     * @author Hannah Schellekens
     */
    object Pattern {

        @JvmField val ellipsis = Regex("""(?<!\.)(\.\.\.)(?!\.)""")

        /**
         * This is the only correct way of using en dashes.
         */
        @JvmField val correctEnDash = RegexPattern.compile("[0-9]+--[0-9]+")!!

        /**
         * Matches the prefix of a label. Amazing comment this is right?
         */
        @JvmField val labelPrefix = RegexPattern.compile(".*:")!!

        /**
         * Matches the end of a sentence.
         *
         * Includes `[^.][^.]` because of abbreviations (at least in Dutch) like `s.v.p.`
         */
        @JvmField val sentenceEnd = RegexPattern.compile("([^.A-Z][^.A-Z][.?!;;] +[^%])|(^\\. )")!!

        /**
         * Matches all interpunction that marks the end of a sentence.
         */
        @JvmField val sentenceSeperator = RegexPattern.compile("[.?!;;]")!!

        /**
         * Matches when a string ends with a non breaking space.
         */
        @JvmField val endsWithNonBreakingSpace = RegexPattern.compile("~$")!!

        /**
         * Finds all abbreviations that have at least two letters separated by comma's.
         *
         * It might be more parts, like `b.v.b.d.` is a valid abbreviation. Likewise are `sajdflkj.asdkfj.asdf` and
         * `i.e.`. Single period abbreviations are not being detected as they can easily be confused with two letter words
         * at the end of the sentece (also localisation...) For this there is a quickfix in [LatexLineBreakInspection].
         */
        @JvmField val abbreviation = RegexPattern.compile("[0-9A-Za-z.]+\\.[A-Za-z](\\.|\\s)")!!

        /**
         * Matches leading and trailing whitespace on a string.
         */
        @JvmField val excessWhitespace = RegexPattern.compile("(^(\\s+).*(\\s*)\$)|(^(\\s*).*(\\s+)\$)")!!

        /**
         * Matches a non-ASCII character.
         */
        @JvmField val nonAscii = RegexPattern.compile("\\P{ASCII}")!!

        /**
         * Seperator for multiple parameter values in one parameter.
         *
         * E.g. when you have \cite{citation1,citation2,citation3}, this pattern will match the separating
         * comma.
         */
        @JvmField val parameterSplit = RegexPattern.compile("\\s*,\\s*")!!

        /**
         * Matches the extension of a file name.
         */
        @JvmField val fileExtension = RegexPattern.compile("\\.[a-zA-Z0-9]+$")!!

        /**
         * Matches all leading whitespace.
         */
        @JvmField val leadingWhitespace = RegexPattern.compile("^\\s*")!!

        /**
         * Matches newlines.
         */
        @JvmField val newline = RegexPattern.compile("\\n")!!

        /**
         * Checks if the string is `text`, two newlines, `text`.
         */
        @JvmField val containsMultipleNewlines = RegexPattern.compile("[^\\n]*\\n\\n+[^\\n]*")!!

        /**
         * Matches a LaTeX command that is the start of an \if-\fi structure.
         */
        @JvmField val ifCommand = RegexPattern.compile("\\\\if[a-zA-Z@]*")!!

        /**
         * Matches the begin and end commands of the cases and split environments.
         */
        @JvmField val casesOrSplitCommands = Regex("((?=\\\\begin\\{cases})|(?<=\\\\begin\\{cases}))" +
                "|((?=\\\\end\\{cases})|(?<=\\\\end\\{cases}))" +
                "|((?=\\\\begin\\{split})|(?<=\\\\begin\\{split}))" +
                "|((?=\\\\end\\{split})|(?<=\\\\end\\{split}))")
    }

    /**
     * @author Hannah Schellekens
     */
    object File {

        /**
         * All file extensions of files that can be included.
         */
        @JvmField val includeExtensions = hashSetOf("tex", "sty", "cls", "bib")

        /**
         * All possible file types.
         */
        @JvmField val fileTypes = setOf(
                BibtexFileType,
                ClassFileType,
                LatexFileType,
                StyleFileType,
                TikzFileType
        )

        /**
         * All file extensions that have to be deleted when clearing auxiliary files.
         *
         * This list is the union of @generated_exts in latexmk and the defined Auxiliary files in TeXWorks.
         * (https://github.com/TeXworks/texworks/blob/9c8cc8b88505103cb8f43fe4105638c77c7e7303/res/resfiles/configuration/texworks-config.txt#L37).
         */
        @JvmField val auxiliaryFileTypes = arrayOf("aux", "bbl", "bcf", "brf", "fls", "idx", "ind", "lof", "lot", "nav", "out", "snm", "toc")
    }

    /**
     * @author Hannah Schellekens
     */
    object Package {

        /**
         * All unicode enabling packages.
         */
        @JvmField val unicode = hashSetOf(
                LatexPackage.INPUTENC.with("utf8"),
                LatexPackage.FONTENC.with("T1")
        )
    }

    /**
     * @author Hannah Schellekens
     */
    object Icon {

        /**
         * Maps file extentions to their corresponding icons.
         */
        @JvmField val fileIcons = mapOf(
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
}