package nl.rubensten.texifyidea.lang.magic

import nl.rubensten.texifyidea.run.LatexCompiler

/**
 * @author Ruben Schellekens
 */
enum class DefaultMagicKeys(
        override val key: String,
        override val documentation: String,
        override val targets: Set<MagicCommentScope> = MagicCommentScope.ALL_SCOPES
) : MagicKey<String> {

    // TeXworks-esque relevant keys.
    PROGRAM("Program", """
        The name of the typesetting engine to use for the current file.
        The following programs are supported:
        ${LatexCompiler.values().joinToString(", ") { it.executableName }}
    """.trimIndent().trim(), MagicCommentScope.FILE.singleScope()),

    ROOT("Root", """
        Indicates that the file with the given name (relative to this file) is the root file of your document.
    """.trimIndent().trim(), MagicCommentScope.FILE.singleScope()),

    // TeXiFy functionality.
    SUPPRESS("Suppress", """
        Denotes that an inspection in the following scope must be suppressed.
        A value of 'All' means that all inspections will be suppressed.
        You can suppress multiple inspections by separating the name of inspections by a comma.
    """.trimIndent().trim()),

    // TeXdoc.
    INFO("Info", """
        A documentation description for a definition.
    """.trimIndent().trim(), MagicCommentScope.COMMAND.singleScope()),

    PARAM("Param", """
        Adds documentation for the next required parameter.
        The first usage of 'Param' will document the 1st parameter, the second usage of 'Param' the 2nd etc.
    """.trimIndent().trim(), MagicCommentScope.COMMAND.singleScope()),

    OPTIONAL("Optional", """
        Adds documentation for the next optional parameter.
        The first usage of 'Optional' will document the 1st parameter, the second usage of 'Optional' the 2nd etc.
        The value starts with the format <tt>fieldname=defaultvalue</tt>.
        After a separating space the documentation description starts.
    """.trimIndent().trim(), MagicCommentScope.COMMAND.singleScope()),

    SINCE("Since", """
        The version number that denotes since when the feature is available.
    """.trimIndent().trim(), MagicCommentScope.COMMAND.singleScope()),

    AUTHOR("Author", """
        The name of the author.
    """.trimIndent().trim(), MagicCommentScope.COMMAND.singleScope()),

    SEE("See", """
        A comma separated list of command names (including backslash) and environments to link to in the documentation.
        These commands and environments are related to the documented command or environment.
    """.trimIndent().trim(), MagicCommentScope.COMMAND.singleScope());

    override fun toString(): String = key
}