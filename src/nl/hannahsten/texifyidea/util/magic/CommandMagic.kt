@file:Suppress("MemberVisibilityCanBePrivate")

package nl.hannahsten.texifyidea.util.magic

import com.intellij.ui.Gray
import nl.hannahsten.texifyidea.lang.commands.*
import nl.hannahsten.texifyidea.lang.commands.LatexBiblatexCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexGenericMathCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexGlossariesCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexListingCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexMathtoolsRegularCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexNatbibCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexNewDefinitionCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexOperatorCommand.*
import nl.hannahsten.texifyidea.lang.commands.LatexUncategorizedStmaryrdSymbols.BIG_SQUARE_CAP
import nl.hannahsten.texifyidea.lang.commands.LatexXparseCommand.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic.stylePrimitives

object CommandMagic {

    /**
     * LaTeX commands that make the text take up more vertical space.
     */
    val high = hashSetOf(
        FRAC.cmd, DFRAC.cmd, SQUARE_ROOT.cmd, SUM.cmd, INTEGRAL.cmd, DOUBLE_INTEGRAL.cmd, TRIPLE_INTEGRAL.cmd,
        QUADRUPLE_INTEGRAL.cmd, N_ARY_PRODUCT.cmd, N_ARY_UNION.cmd, N_ARY_INTERSECTION.cmd,
        N_ARY_SQUARE_UNION.cmd, BIG_SQUARE_CAP.cmd
    )

    /**
     * Level of labeled commands.
     */
    val labeledLevels: Map<LatexCommand, Int> = mapOf(
        // See page 23 of the LaTeX Companion
        PART to -1, // actually, it is level 0 in classes that do not define \chapter and -1 in book and report
        CHAPTER to 0,
        SECTION to 1,
        SUBSECTION to 2,
        SUBSUBSECTION to 3,
        PARAGRAPH to 4,
        SUBPARAGRAPH to 5
    )

    /** Section commands sorted from large to small. */
    val sectioningCommands = labeledLevels.entries.sortedBy { it.value }.map { it.key }

    /**
     * Commands that define a label via an optional parameter
     */
    @JvmField
    val labelAsParameter = hashSetOf(LSTINPUTLISTING.cmd)

    /**
     * All commands that mark some kind of section.
     */
    val sectionMarkers = listOf(
        PART,
        CHAPTER,
        SECTION,
        SUBSECTION,
        SUBSUBSECTION,
        PARAGRAPH,
        SUBPARAGRAPH
    ).map { it.cmd }

    /**
     * The colours that each section separator has.
     */
    val sectionSeparatorColors = mapOf(
        PART.cmd to Gray._152,
        CHAPTER.cmd to Gray._172,
        SECTION.cmd to Gray._182,
        SUBSECTION.cmd to Gray._202,
        SUBSUBSECTION.cmd to Gray._212,
        PARAGRAPH.cmd to Gray._222,
        SUBPARAGRAPH.cmd to Gray._232
    )

    /**
     * LaTeX commands that increase a counter that can be labeled.
     */
    val increasesCounter =
        hashSetOf(CAPTION.cmd, CAPTIONOF.cmd, CHAPTER.cmd, SECTION.cmd, SUBSECTION.cmd, ITEM.cmd, LSTINPUTLISTING.cmd)

    /**
     * All commands that represent a reference to a label, excluding user defined commands.
     */
    val labelReferenceWithoutCustomCommands = LatexRegularCommand.ALL
        .filter { cmd -> cmd.arguments.any { it.type == Argument.Type.LABEL } }
        .map { it.cmd }.toSet()

    /**
     * All commands that represent a reference to a bibliography entry/item.
     */
    val bibliographyReference = hashSetOf(
        CITE,
        NOCITE,
        CITEP,
        CITEP_STAR,
        CITET,
        CITET_STAR,
        CITEP,
        CITEP_STAR_CAPITALIZED,
        CITET_CAPITALIZED,
        CITET_STAR_CAPITALIZED,
        CITEALP,
        CITEALP_STAR,
        CITEALT,
        CITEALT_STAR,
        CITEALP_CAPITALIZED,
        CITEALP_STAR_CAPITALIZED,
        CITEALT_CAPITALIZED,
        CITEALT_STAR_CAPITALIZED,
        CITEAUTHOR,
        CITEAUTHOR_STAR,
        CITEAUTHOR_CAPITALIZED,
        CITEAUTHOR_STAR_CAPITALIZED,
        CITEYEAR,
        CITEYEARPAR,
        PARENCITE,
        PARENCITE_CAPITALIZED,
        FOOTCITE,
        FOOTCITETEXT,
        TEXTCITE,
        TEXTCITE_CAPITALIZED,
        SMARTCITE,
        SMARTCITE_CAPITALIZED,
        CITE_STAR,
        PARENCITE_STAR,
        SUPERCITE,
        CITES,
        CITES_CAPITALIZED,
        PARENCITES,
        PARENCITES_CAPITALIZED,
        FOOTCITES,
        FOOTCITETEXTS,
        SMARTCITES,
        SMARTCITES_CAPITALIZED,
        TEXTCITES,
        TEXTCITES_CAPITALIZED,
        SUPERCITES,
        AUTOCITE,
        AUTOCITE_CAPITALIZED,
        AUTOCITE_STAR,
        AUTOCITE_STAR_CAPITALIZED,
        AUTOCITES,
        AUTOCITES_CAPITALIZED,
        CITETITLE,
        CITETITLE_STAR,
        CITEYEAR_STAR,
        CITEDATE,
        CITEDATE_STAR,
        CITEURL,
        VOLCITE,
        VOLCITE_CAPITALIZED,
        VOLCITES,
        VOLCITES_CAPITALIZED,
        PVOLCITE,
        PVOLCITE_CAPITALIZED,
        PVOLCITES,
        PVOLCITES_CAPITALIZED,
        FVOLCITE,
        FVOLCITE_CAPITALIZED,
        FTVOLCITE,
        FTVOLCITE_CAPITALIZED,
        FVOLCITES,
        FVOLCITES_CAPITALIZED,
        FTVOLCITES,
        SVOLCITE,
        SVOLCITE_CAPITALIZED,
        SVOLCITES,
        SVOLCITES_CAPITALIZED,
        TVOLCITE,
        TVOLCITE_CAPITALIZED,
        TVOLCITES,
        TVOLCITES_CAPITALIZED,
        AVOLCITE,
        AVOLCITE_CAPITALIZED,
        AVOLCITES,
        AVOLCITES_CAPITALIZED,
        FULLCITE,
        FOOTFULLCITE,
        NOTECITE,
        NOTECITE_CAPITALIZED,
        PNOTECITE,
        FNOTECITE
    ).map { it.cmd }.toSet()

    /**
     * All commands that define a glossary entry of the glossaries package (e.g. \newacronym).
     * When adding a command, define how to get the glossary name in [nl.hannahsten.texifyidea.lang.commands.LatexGlossariesCommand.extractGlossaryName].
     */
    val glossaryEntry =
        hashSetOf(NEWGLOSSARYENTRY, LONGNEWGLOSSARYENTRY, NEWACRONYM, NEWABBREVIATION, NEWACRO, ACRO, ACRODEF).map { it.cmd }.toSet()

    /**
     * All commands that reference a glossary entry from the glossaries package (e.g. \gls).
     */
    val glossaryReference = LatexGlossariesCommand.entries.filter { cmd -> cmd.arguments.any { it.name == "label" || it.name == "acronym" } }.map { it.cmd }.toSet()

    /**
     * All commands that represent some kind of reference (think \ref and \cite).
     */
    val reference = labelReferenceWithoutCustomCommands + bibliographyReference

    /**
     * Commands from the import package which require an absolute path as first parameter.
     */
    val absoluteImportCommands = setOf(INCLUDEFROM.cmd, INPUTFROM.cmd, IMPORT.cmd)

    /**
     * Commands from the import package which require a relative path as first parameter.
     */
    val relativeImportCommands = setOf(SUBIMPORT.cmd, SUBINPUTFROM.cmd, SUBINCLUDEFROM.cmd)

    /**
     * All commands that define labels and that are present by default.
     * To include user defined commands, use [nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands] (may be significantly slower).
     */
    val labelDefinitionsWithoutCustomCommands = setOf(LABEL.cmd)

    /**
     * All commands that define bibliography items.
     */
    val bibliographyItems = setOf(BIBITEM.cmd)

    /**
     * All math operators without a leading slash.
     *
     * Reference [Unofficial LaTeX2e reference manual](https://latexref.xyz/Math-functions.html)
     */
    @JvmField
    val slashlessMathOperators = hashSetOf(
        INVERSE_COSINE, INVERSE_SINE, INVERSE_TANGENT, ARGUMENT, BMOD, COSINE, HYPERBOLIC_COSINE, COTANGENT,
        HYPERBOLIC_COTANGENT, COSECANT, DEGREES, DERMINANT, DIMENSION, EXPONENTIAL, GREATEST_COMMON_DIVISOR,
        HOMOMORPHISM, INFINUM, KERNEL, BASE_2_LOGARITHM, LIMIT, LIMIT_INFERIOR, LIMIT_SUPERIOR,
        NATURAL_LOGARITHM, LOGARITHM, MAXIMUM, MINIMUM, PMOD, PROBABILITY, SECANT, SINE,
        HYPERBOLIC_SINE, SUPREMUM, TANGENT, HBOLICTANGENT
    )

    /**
     * All commands that define regular commands, and that require that the command is not already defined.
     */
    val regularStrictCommandDefinitions = hashSetOf(
        NEWCOMMAND.cmd,
        NEWCOMMAND_STAR.cmd,
        NEWIF.cmd,
        NEWDOCUMENTCOMMAND.cmd,
        NEWCOMMANDX.cmd,
    )

    /**
     * Commands that define other command but don't complain if it is already defined.
     */
    val flexibleCommandDefinitions = setOf(
        PROVIDECOMMAND, // Does nothing if command exists
        PROVIDECOMMAND_STAR,
        PROVIDEDOCUMENTCOMMAND, // Does nothing if command exists
        DECLAREDOCUMENTCOMMAND,
        DEF,
        LET,
        PROVIDECOMMANDX,
        DECLAREROBUSTCOMMANDX,
    ).map { it.cmd }

    /**
     * All commands that define or redefine other commands, whether it exists or not.
     */
    val commandRedefinitions = setOf(
        RENEWCOMMAND,
        RENEWCOMMAND_STAR,
        CATCODE, // Not really redefining commands, but characters
        RENEWCOMMANDX,
    ).map { it.cmd } + flexibleCommandDefinitions

    /**
     * All commands that define or redefine regular commands.
     */
    val regularCommandDefinitionsAndRedefinitions = regularStrictCommandDefinitions + commandRedefinitions

    /**
     * All commands that define commands that should be used exclusively
     * in math mode.
     */
    val mathCommandDefinitions = hashSetOf(
        DECLARE_MATH_OPERATOR.cmd,
        DECLARE_PAIRED_DELIMITER.cmd,
        DECLARE_PAIRED_DELIMITER_X.cmd,
        DECLARE_PAIRED_DELIMITER_XPP.cmd
    )

    /**
     * All commands that can define regular commands.
     */
    val commandDefinitions = regularStrictCommandDefinitions + mathCommandDefinitions + flexibleCommandDefinitions

    /**
     * All commands that (re)define new commands.
     */
    val commandDefinitionsAndRedefinitions = regularCommandDefinitionsAndRedefinitions + mathCommandDefinitions

    /**
     * All commands that define new documentclasses.
     */
    val classDefinitions = hashSetOf(PROVIDESCLASS.cmd)

    /**
     * All commands that define new packages.
     */
    val packageDefinitions = hashSetOf(PROVIDESPACKAGE.cmd)

    /**
     * All commands that define new environments.
     */
    val environmentDefinitions = hashSetOf(
        NEWENVIRONMENT,
        NEWTHEOREM,
        NEWDOCUMENTENVIRONMENT,
        PROVIDEDOCUMENTENVIRONMENT,
        DECLAREDOCUMENTENVIRONMENT,
        NEWTCOLORBOX,
        DECLARETCOLORBOX,
        NEWTCOLORBOX_,
        PROVIDETCOLORBOX,
        NEWENVIRONMENTX,
        LSTNEWENVIRONMENT,
    ).map { it.cmd }

    /**
     * All commands that define or redefine other environments, whether it exists or not.
     */
    val environmentRedefinitions = hashSetOf(
        RENEWENVIRONMENT.cmd,
        RENEWTCOLORBOX.cmd,
        RENEWTCOLORBOX_.cmd,
        RENEWENVIRONMENTX.cmd,
    )

    /**
     * All commands that define stuff like classes, environments, and definitions.
     */
    val definitions = commandDefinitionsAndRedefinitions + classDefinitions + packageDefinitions + environmentDefinitions

    /**
     * Commands for which TeXiFy-IDEA has essential custom behaviour and which should not be redefined.
     */
    val fragile = hashSetOf(
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

    val graphicPathsCommands = listOf(GRAPHICSPATH, SVGPATH)

    /**
     * Commands that should not have the given file extensions.
     */
    val illegalExtensions = mapOf(
        INPUT.cmd to listOf(".tex"),
        INCLUDE.cmd to listOf(".tex"),
        INCLUDESTANDALONE.cmd to listOf(".tex") + FileMagic.graphicFileExtensions.map { ".$it" },
        SUBFILEINCLUDE.cmd to listOf(".tex"),
        BIBLIOGRAPHY.cmd to listOf(".bib"),
        INCLUDEGRAPHICS.cmd to FileMagic.graphicFileExtensions.map { ".$it" }, // https://tex.stackexchange.com/a/1075/98850
        USEPACKAGE.cmd to listOf(".sty"),
        EXTERNALDOCUMENT.cmd to listOf(".tex"),
        TIKZFIG.cmd to listOf("tikz"),
        CTIKZFIG.cmd to listOf("tikz"),
    )

    /**
     * Commands which can include packages in optional or required arguments.
     */
    val packageInclusionCommands = setOf(
        USEPACKAGE, REQUIREPACKAGE, DOCUMENTCLASS, LOADCLASS
    ).map { it.cmd }.toSet()

    val tikzLibraryInclusionCommands = setOf(USETIKZLIBRARY.cmd)

    val pgfplotsLibraryInclusionCommands = setOf(USEPGFPLOTSLIBRARY.cmd)

    /**
     * Commands that should have the given file extensions.
     */
    val requiredExtensions = mapOf(
        ADDBIBRESOURCE.cmd to listOf("bib")
    )

    /**
     * Extensions that should only be scanned for the provided include commands.
     */
    val includeOnlyExtensions: Map<String, Set<String>> = mapOf(
        INCLUDE.cmd to hashSetOf("tex"),
        INCLUDEONLY.cmd to hashSetOf("tex"),
        SUBFILE.cmd to hashSetOf("tex"),
        SUBFILEINCLUDE.cmd to hashSetOf("tex"),
        BIBLIOGRAPHY.cmd to hashSetOf("bib"),
        ADDBIBRESOURCE.cmd to hashSetOf("bib"),
        REQUIREPACKAGE.cmd to hashSetOf("sty"),
        USEPACKAGE.cmd to hashSetOf("sty"),
        DOCUMENTCLASS.cmd to hashSetOf("cls"),
        LOADCLASS.cmd to hashSetOf("cls"),
        EXTERNALDOCUMENT.cmd to hashSetOf("tex") // Not completely true, as it only includes labels. Technically it references an aux file
    )

    /**
     * Commands that include bib files.
     */
    val bibliographyIncludeCommands = includeOnlyExtensions.entries.filter { it.value.contains("bib") }.map { it.key }

    /**
     * All commands that at first glance look like \if-esque commands, but that actually aren't.
     */
    val ignoredIfs = hashSetOf("\\newif", "\\iff", "\\ifthenelse", "\\iftoggle", "\\ifoot", "\\ifcsvstrcmp")

    /**
     * List of all TeX style primitives.
     */
    val stylePrimitives = listOf(
        RM.cmd, SF.cmd, TT.cmd, IT.cmd, SL.cmd, SC.cmd, BF.cmd
    )

    /**
     * The LaTeX counterparts of all [stylePrimitives] commands.
     */
    val stylePrimitiveReplacements = mapOf(
        RM.cmd to "\\textrm", SF.cmd to "\\textsf", TT.cmd to "\\texttt", IT.cmd to "\\textit",
        SL.cmd to "\\textsl", SC.cmd to "\\textsc", BF.cmd to "\\textbf"
    )

    /**
     * Set of text styling commands
     */
    val textStyles = setOf(
        TEXTRM, TEXTSF, TEXTTT, TEXTIT,
        TEXTSL, TEXTSC, TEXTBF, EMPH,
        TEXTUP, TEXTMD
    ).map { it.cmd }

    /**
     * All LaTeX commands that contain a url (in their first parameter).
     */
    val urls = hashSetOf(URL.cmd, HREF.cmd)

    /**
     * All BibTeX tags that take a url as their parameter.
     */
    val bibUrls = hashSetOf("url", "biburl")

    /**
     * Commands that always contain a certain language.
     *
     * Maps the name of the command to the registered Language id.
     */
    val languageInjections = hashMapOf(
        DIRECTLUA.cmd to "Lua",
        LUAEXEC.cmd to "Lua"
    )

    /**
     * Commands that have a verbatim argument.
     *
     * Maps a command to a boolean that is true when the required argument can be specified with any pair of characters.
     */
    val verbatim = hashMapOf(
        "verb" to true,
        "verb*" to true,
        "directlua" to false,
        "luaexec" to false,
        "lstinline" to true
    )

    /**
     *
     */
    val foldableFootnotes = listOf(
        FOOTNOTE.cmd, FOOTCITE.cmd
    )

    /**
     * Commands that should be contributed to the to do toolwindow.
     */
    val todoCommands = setOf(LatexTodoCommand.TODO.cmd, LatexTodoCommand.MISSINGFIGURE.cmd)
}