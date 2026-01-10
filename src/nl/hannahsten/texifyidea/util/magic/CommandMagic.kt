@file:Suppress("MemberVisibilityCanBePrivate")

package nl.hannahsten.texifyidea.util.magic

import com.intellij.ui.Gray
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LatexContext
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdFiles
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdMathSymbols
import nl.hannahsten.texifyidea.util.magic.CommandMagic.stylePrimitives

object CommandMagic {

    /**
     * LaTeX commands that make the text take up more vertical space.
     */
    val high = CommandNames.run {
        hashSetOf(
            FRAC, DFRAC, SQRT, SUM, INT, IINT, IIINT, IIIINT,
            PROD, BIGCUP, BIGCAP, BIGSQCUP, BIGVEE, BIGWEDGE
        )
    }

    /**
     * Map of `\section`-like commands to their level.
     */
    val sectionNameToLevel: Map<String, Int> = CommandNames.run {
        mapOf(
            PART to -1, // actually, it is level 0 in classes that do not define \chapter and -1 in book and report
            CHAPTER to 0,
            SECTION to 1,
            SUB_SECTION to 2,
            SUB_SUB_SECTION to 3,
            PARAGRAPH to 4,
            SUB_PARAGRAPH to 5,
        )
    }

    /**
     * Commands that define a label via an optional parameter
     */
    @JvmField
    val labelAsParameter = hashSetOf(CommandNames.LSTINPUTLISTING)

    /**
     * The colours that each section separator has.
     */
    val sectionSeparatorColors = CommandNames.run {
        mapOf(
            PART to Gray._132,
            CHAPTER to Gray._152,
            SECTION to Gray._172,
            SUB_SECTION to Gray._182,
            SUB_SUB_SECTION to Gray._202,
            PARAGRAPH to Gray._222,
            SUB_PARAGRAPH to Gray._232
        )
    }

    private fun allCommandsWithContext(context: LatexContext): Map<String, LSemanticCommand> = AllPredefined.findCommandsByContext(context).associateBy {
        it.nameWithSlash
    }

    /**
     * All commands that represent a reference to some label.
     */
    val labelReference: Map<String, LSemanticCommand> = allCommandsWithContext(LatexContexts.LabelReference)

    /**
     * All commands that represent a reference to a bibliography entry/item.
     */
    val bibliographyReference: Map<String, LSemanticCommand> =
        allCommandsWithContext(LatexContexts.BibReference)

    /**
     * All commands that define a glossary entry of the glossaries package (e.g. \newacronym).
     * When adding a command, define how to get the glossary name in [nl.hannahsten.texifyidea.lang.commands.LatexGlossariesCommand.extractGlossaryName].
     */
    val glossaryEntry: Map<String, LSemanticCommand> =
        allCommandsWithContext(LatexContexts.GlossaryDefinition)

    /**
     * All commands that reference a glossary entry from the glossaries package (e.g. \gls).
     */
    val glossaryReference: Map<String, LSemanticCommand> =
        allCommandsWithContext(LatexContexts.GlossaryReference)

    /**
     * All commands that represent some kind of reference (think \ref and \cite).
     */
    val reference: Set<String> = labelReference.keys + bibliographyReference.keys

    /**
     * Commands from the import package which require an absolute path as first parameter.
     */
    val absoluteImportCommands: Set<String> = CommandNames.run {
        setOf(INCLUDE_FROM, INPUT_FROM, IMPORT)
    }

    /**
     * Commands from the import package which require a relative path as first parameter.
     */
    val relativeImportCommands: Set<String> =
        CommandNames.run {
            setOf(SUB_IMPORT, SUB_INPUT_FROM, SUB_INCLUDE_FROM)
        }

    /**
     * All commands that define labels and that are present by default.
     *
     * Consider migrating to context-aware approach, using [nl.hannahsten.texifyidea.index.LatexDefinitionService].
     */
    val labels = CommandNames.run { setOf(LABEL) }

    /**
     * All math operators without a leading slash.
     *
     * Reference [Unofficial LaTeX2e reference manual](https://latexref.xyz/Math-functions.html)
     */
    val mathOperators: List<LSemanticCommand>
        get() = PredefinedCmdMathSymbols.mathTextOperators

    /**
     * All commands that define regular commands, and that require that the command is not already defined.
     */
    val regularStrictCommandDefinitions: Set<String> = CommandNames.run {
        hashSetOf(
            NEW_COMMAND,
            NEW_COMMAND_STAR,
            NEW_IF,
            NEW_DOCUMENT_COMMAND,
            NEW_COMMAND_X,
        )
    }

    /**
     * Commands that define other command but don't complain if it is already defined.
     */
    val flexibleCommandDefinitions: Set<String> = CommandNames.run {
        setOf(
            PROVIDE_COMMAND, // Does nothing if command exists
            PROVIDE_COMMAND_STAR,
            PROVIDE_DOCUMENT_COMMAND, // Does nothing if command exists
            DECLARE_DOCUMENT_COMMAND,
            DEF,
            LET,
            PROVIDE_COMMAND_X,
            DECLARE_ROBUST_COMMAND_X,
        )
    }

    /**
     * All commands that define or redefine other commands, whether it exists or not.
     */
    val commandRedefinitions: Set<String> = CommandNames.run {
        setOf(
            RENEW_COMMAND,
            RENEW_COMMAND_STAR,
            CAT_CODE, // Not really redefining commands, but characters
            RENEW_COMMAND_X,
        ) + flexibleCommandDefinitions
    }

    /**
     * All commands that define or redefine regular commands.
     */
    val regularCommandDefinitionsAndRedefinitions: Set<String> = regularStrictCommandDefinitions + commandRedefinitions

    /**
     * All commands that define commands that should be used exclusively
     * in math mode.
     */
    val mathCommandDefinitions = CommandNames.run {
        hashSetOf(
            DECLARE_MATH_OPERATOR,
            DECLARE_PAIRED_DELIMITER,
            DECLARE_PAIRED_DELIMITER_X,
            DECLARE_PAIRED_DELIMITER_XPP
        )
    }

    /**
     * All the commands that may define regular commands, whether it exists or not.
     */
    val allFileIncludeCommands: Set<String> = PredefinedCmdFiles.allCommands.map { it.nameWithSlash }.toSet()

    /**
     * All commands that (re)define new commands.
     */
    val commandDefinitionsAndRedefinitions: Set<String> = regularCommandDefinitionsAndRedefinitions + mathCommandDefinitions

    /**
     * All commands that define new documentclasses.
     */
    val classDefinitions = CommandNames.run { hashSetOf(PROVIDES_CLASS) }

    /**
     * All commands that define new packages.
     */
    val packageDefinitions = CommandNames.run { hashSetOf(PROVIDES_PACKAGE) }

    /**
     * All commands that define new environments.
     */
    val environmentDefinitions: Set<String> = CommandNames.run {
        hashSetOf(
            NEW_ENVIRONMENT,
            NEW_THEOREM,
            NEW_DOCUMENT_ENVIRONMENT,
            PROVIDE_DOCUMENT_ENVIRONMENT,
            DECLARE_DOCUMENT_ENVIRONMENT,
            NEW_TCOLORBOX,
            DECLARE_TCOLORBOX,
            NEW_TCOLORBOX_CAP,
            PROVIDE_TCOLORBOX,
            NEW_ENVIRONMENT_X,
            LST_NEW_ENVIRONMENT,
        )
    }

    /**
     * All commands that define or redefine other environments, whether it exists or not.
     */
    val environmentRedefinitions = CommandNames.run {
        hashSetOf(
            RENEW_ENVIRONMENT,
            RENEW_TCOLORBOX,
            RENEW_TCOLORBOX_CAP,
            RENEW_ENVIRONMENT_X,
        )
    }

    /**
     * All commands that define stuff like classes, environments, and definitions.
     */
    val definitions: Set<String> = commandDefinitionsAndRedefinitions + classDefinitions + packageDefinitions + environmentDefinitions + environmentRedefinitions

    /**
     * Commands for which TeXiFy-IDEA has essential custom behaviour and which should not be redefined.
     */
    val fragile: Set<String> = CommandNames.run {
        hashSetOf(
            ADD_TO_COUNTER, BEGIN, CHAPTER, DEF, DOCUMENT_CLASS, END,
            INCLUDE, INCLUDE_ONLY, INPUT, LABEL, LET, NEW_COMMAND,
            OVERLINE, PARAGRAPH, PART, RENEW_COMMAND, SECTION, SET_COUNTER,
            SOUT, SUB_PARAGRAPH, SUB_SECTION, SUB_SUB_SECTION, TEXT_BF,
            TEXT_IT, TEXT_SC, TEXT_SL, TEXT_TT, UNDERLINE, L_BRACKET, R_BRACKET,
            NEW_ENVIRONMENT, BIB_ITEM,
            NEW_DOCUMENT_COMMAND,
            PROVIDE_DOCUMENT_COMMAND,
            DECLARE_DOCUMENT_COMMAND,
            NEW_DOCUMENT_ENVIRONMENT,
            PROVIDE_DOCUMENT_ENVIRONMENT,
            DECLARE_DOCUMENT_ENVIRONMENT
        )
    }

    val graphicPathsCommandNames = setOf(
        CommandNames.GRAPHICS_PATH,
        CommandNames.SVG_PATH
    )

    val graphicLibs: Set<LatexLib> = setOf(LatexLib.GRAPHICX, LatexLib.SVG)

    /**
     * Commands that should not have the given file extensions.
     */
    val illegalExtensions = CommandNames.run {
        mapOf(
            INPUT to listOf(".tex"),
            INCLUDE to listOf(".tex"),
            INCLUDE_STANDALONE to listOf(".tex") + FileMagic.graphicFileExtensions.map { ".$it" },
            SUBFILE_INCLUDE to listOf(".tex"),
            BIBLIOGRAPHY to listOf(".bib"),
            INCLUDE_GRAPHICS to FileMagic.graphicFileExtensions.map { ".$it" }, // https://tex.stackexchange.com/a/1075/98850
            USE_PACKAGE to listOf(".sty"),
            EXTERNAL_DOCUMENT to listOf(".tex"),
            TIKZ_FIG to listOf("tikz"),
            C_TIKZ_FIG to listOf("tikz"),
        )
    }

    /**
     * Commands which can include packages in optional or required arguments.
     */
    val packageInclusionCommands = CommandNames.run {
        setOf(USE_PACKAGE, REQUIRE_PACKAGE, DOCUMENT_CLASS, LOAD_CLASS, LOAD_CLASS_WITH_OPTIONS)
    }

    val tikzLibraryInclusionCommands = CommandNames.run { setOf(USE_TIKZ_LIBRARY) }

    val pgfplotsLibraryInclusionCommands = CommandNames.run { setOf(USE_PGF_PLOTS_LIBRARY) }

    /**
     * Commands that should have the given file extensions.
     */
    val requiredExtensions = CommandNames.run {
        mapOf(ADD_BIB_RESOURCE to listOf("bib"))
    }

    /**
     * Extensions that should only be scanned for the provided include commands.
     */
    val includeAndExtensions: Map<String, Set<String>> = CommandNames.run {
        mapOf(
            INPUT to hashSetOf("tex"),
            INCLUDE to hashSetOf("tex"),
            INCLUDE_ONLY to hashSetOf("tex"),
            SUBFILE to hashSetOf("tex"),
            SUBFILE_INCLUDE to hashSetOf("tex"),
            BIBLIOGRAPHY to hashSetOf("bib"),
            ADD_BIB_RESOURCE to hashSetOf("bib"),
            REQUIRE_PACKAGE to hashSetOf("sty"),
            USE_PACKAGE to hashSetOf("sty"),
            DOCUMENT_CLASS to hashSetOf("cls"),
            LOAD_CLASS to hashSetOf("cls"),
            EXTERNAL_DOCUMENT to hashSetOf("tex")
        )
    }

    /**
     * Commands that include bib files.
     */
    val bibliographyIncludeCommands: Set<String> = includeAndExtensions.entries.filter { it.value.contains("bib") }.map { it.key }.toSet()

    /**
     * All commands that at first glance look like \if-esque commands, but that actually aren't.
     */
    val ignoredIfs: Set<String> = CommandNames.run {
        hashSetOf(NEW_IF, IFF, IF_THEN_ELSE, IF_TOGGLE, IFOOT, IF_CSVSTRCMP)
    }

    /**
     * List of all TeX style primitives.
     */
    val stylePrimitives: Set<String> = CommandNames.run {
        setOf(RM, SF, TT, IT, SL, SC, BF)
    }

    /**
     * The LaTeX counterparts of all [stylePrimitives] commands.
     */
    val stylePrimitiveReplacements: Map<String, String> = CommandNames.run {
        mapOf(
            RM to TEXT_RM, SF to TEXT_SF, TT to TEXT_TT, IT to TEXT_IT,
            SL to TEXT_SL, SC to TEXT_SC, BF to TEXT_BF
        )
    }

    /**
     * All BibTeX tags that take a url as their parameter.
     */
    val bibUrls = hashSetOf("url", "biburl")

    /**
     * Commands that always contain a certain language.
     *
     * Maps the name of the command to the registered Language id.
     */
    val languageInjections: Map<String, String> = hashMapOf(
        CommandNames.DIRECT_LUA to "Lua",
        CommandNames.LUA_EXEC to "Lua",
    )

    /**
     * Commands that have a verbatim argument.
     *
     * Maps a command to a boolean that is true when the required argument can be specified with any pair of characters.
     */
    val verbatim: Map<String, Boolean> = hashMapOf(
        "verb" to true,
        "verb*" to true,
        "directlua" to false,
        "luaexec" to false,
        "lstinline" to true
    )

    /**
     * The names of the footnote commands that should be folded
     */
    val foldableFootnotes: Set<String> = CommandNames.run {
        setOf(FOOT_NOTE, FOOT_CITE)
    }

    /**
     * Commands that should be contributed to the to do toolwindow.
     */
    val todoCommands: Set<String> = CommandNames.run {
        setOf(TODO, MISSING_FIGURE)
    }
}