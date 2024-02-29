package nl.hannahsten.texifyidea.lang.magic

import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler

/**
 * @author Hannah Schellekens
 */
enum class DefaultMagicKeys(
    override val key: String,
    val displayKey: String,
    override val documentation: String,
    override val targets: Set<MagicCommentScope> = MagicCommentScope.ALL_SCOPES
) : MagicKey<String> {

    // TeXworks-esque relevant keys.
    COMPILER(
        "compiler",
        "Compiler",
        """
        The name of the typesetting engine to use for the current file.
        The following programs are supported:
        ${LatexCompiler.entries.joinToString(", ") { it.executableName }}
        """.trimIndent().trim(),
        MagicCommentScope.FILE.singleScope()
    ),

    PROGRAM(
        "program",
        "Program",
        """
        The name of the typesetting engine to use for the current file.
        The following programs are supported:
        ${LatexCompiler.entries.joinToString(", ") { it.executableName }}
        """.trimIndent().trim(),
        MagicCommentScope.FILE.singleScope()
    ),

    BIBTEXCOMPILER(
        "bibtex compiler",
        "BibTeX Compiler",
        """
        The name of the typesetting engine to use for the current file.
        The following programs are supported:
        ${
            BibliographyCompiler.entries
            .joinToString(", ") { it.executableName }}
        """.trimIndent().trim(),
        MagicCommentScope.FILE.singleScope()
    ),

    ROOT(
        "root",
        "Root",
        """
        Indicates that the file with the given name (relative to this file) is the root file of your document.
        """.trimIndent().trim(),
        MagicCommentScope.FILE.singleScope()
    ),

    // TeXiFy functionality.
    SUPPRESS(
        "suppress",
        "Suppress",
        """
        Denotes that an inspection in the following scope must be suppressed.
        A value of 'All' means that all inspections will be suppressed.
        You can suppress multiple inspections by separating the name of inspections by a comma.
        """.trimIndent().trim()
    ),

    INJECT_LANGUAGE(
        "language",
        "Language",
        """
        Indicates that a language should be injected in the following environment.
        """.trimIndent().trim(),
        MagicCommentScope.ENVIRONMENT.singleScope()
    ),

    BEGIN_PREAMBLE(
        "begin preamble",
        "Begin preamble",
        """
        Indicates the start of a (part of) preamble to be included in the preview.
        """.trimIndent().trim(),
        MagicCommentScope.FILE.singleScope()
    ),

    END_PREAMBLE(
        "end preamble",
        "End preamble",
        """
        Indicates the end of a (part of) preamble to be included in the preview.
        """.trimIndent().trim(),
        MagicCommentScope.FILE.singleScope()
    ),

    PREVIEW_PREAMBLE(
        "preview preamble",
        "Preview preamble",
        """
        Indicates that this file has to be included in the preamble of the preview.
        """.trimIndent().trim(),
        MagicCommentScope.FILE.singleScope()
    ),

    PARSER(
        "parser",
        "Parser",
        """
        Allows for switching parsing off and on.
        """.trimIndent().trim(),
        MagicCommentScope.FILE.singleScope()
    ),

    // TeXdoc.
    INFO(
        "info",
        "Info",
        """
        A documentation description for a definition.
        """.trimIndent().trim(),
        MagicCommentScope.COMMAND.singleScope()
    ),

    PARAM(
        "param",
        "Param",
        """
        Adds documentation for the next required parameter.
        The first usage of 'Param' will document the 1st parameter, the second usage of 'Param' the 2nd etc.
        """.trimIndent().trim(),
        MagicCommentScope.COMMAND.singleScope()
    ),

    OPTIONAL(
        "optional",
        "Optional",
        """
        Adds documentation for the next optional parameter.
        The first usage of 'Optional' will document the 1st parameter, the second usage of 'Optional' the 2nd etc.
        The value starts with the format <tt>fieldname=defaultvalue</tt>.
        After a separating space the documentation description starts.
        """.trimIndent().trim(),
        MagicCommentScope.COMMAND.singleScope()
    ),

    SINCE(
        "since",
        "Since",
        """
        The version number that denotes since when the feature is available.
        """.trimIndent().trim(),
        MagicCommentScope.COMMAND.singleScope()
    ),

    AUTHOR(
        "author",
        "Author",
        """
        The name of the author.
        """.trimIndent().trim(),
        MagicCommentScope.COMMAND.singleScope()
    ),

    SEE(
        "see",
        "See",
        """
        A comma separated list of command names (including backslash) and environments to link to in the documentation.
        These commands and environments are related to the documented command or environment.
        """.trimIndent().trim(),
        MagicCommentScope.COMMAND.singleScope()
    ),

    FAKE(
        "fake",
        "Fake",
        """
        A comment tdieksoe
        """.trimIndent().trim(),
        MagicCommentScope.COMMAND.singleScope()
    );

    override fun toString(): String = key
}